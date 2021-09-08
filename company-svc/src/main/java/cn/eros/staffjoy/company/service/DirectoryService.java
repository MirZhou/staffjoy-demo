package cn.eros.staffjoy.company.service;

import cn.eros.staffjoy.account.client.AccountClient;
import cn.eros.staffjoy.account.dto.AccountDto;
import cn.eros.staffjoy.account.dto.GenericAccountResponse;
import cn.eros.staffjoy.account.dto.GetOrCreateRequest;
import cn.eros.staffjoy.bot.dto.OnboardWorkerRequest;
import cn.eros.staffjoy.common.api.ResultCode;
import cn.eros.staffjoy.common.auditlog.LogEntry;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.dto.Association;
import cn.eros.staffjoy.company.dto.AssociationList;
import cn.eros.staffjoy.company.dto.DirectoryEntryDto;
import cn.eros.staffjoy.company.dto.DirectoryList;
import cn.eros.staffjoy.company.dto.NewDirectoryEntry;
import cn.eros.staffjoy.company.dto.TeamDto;
import cn.eros.staffjoy.company.dto.WorkerOfList;
import cn.eros.staffjoy.company.model.Directory;
import cn.eros.staffjoy.company.repo.CompanyRepository;
import cn.eros.staffjoy.company.repo.DirectoryRepository;
import cn.eros.staffjoy.company.service.helper.ServiceHelper;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author 周光兵
 * @date 2021/8/27 13:19
 */
@Service
public class DirectoryService {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(DirectoryService.class);

    @Autowired
    private ServiceHelper serviceHelper;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private AccountClient accountClient;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private AdminService adminService;

    @Autowired
    private WorkerService workerService;

    public DirectoryEntryDto createDirectory(NewDirectoryEntry request) {
        boolean companyExist = this.companyRepository.existsById(request.getCompanyId());
        if (!companyExist) {
            throw new ServiceException(ResultCode.NOT_FOUND, "Company with specified id not found.");
        }

        GetOrCreateRequest getOrCreateRequest = GetOrCreateRequest.builder().name(request.getName())
                .email(request.getEmail()).phoneNumber(request.getPhoneNumber()).build();

        GenericAccountResponse accountResponse;
        try {
            accountResponse = this.accountClient.getOrCreateAccount(AuthConstant.AUTHORIZATION_COMPANY_SERVICE,
                    getOrCreateRequest);
        } catch (Exception ex) {
            String errMsg = "Couldn't get or create user";
            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        if (!accountResponse.isSuccess()) {
            this.serviceHelper.handleErrorAndThrowException(LOGGER, accountResponse.getMessage());
            throw new ServiceException(accountResponse.getMessage());
        }

        AccountDto accountDto = accountResponse.getAccount();

        DirectoryEntryDto directoryEntryDto = DirectoryEntryDto.builder().internalId(request.getInternalId())
                .companyId(request.getCompanyId()).build();

        this.copyAccountToDirectory(accountDto, directoryEntryDto);

        boolean directoryExists = this.directoryRepository.findByCompanyIdAndUserId(request.getCompanyId(),
                accountDto.getId()) != null;
        if (directoryExists) {
            throw new ServiceException("relationship already exists");
        }

        Directory directory = Directory.builder().companyId(request.getCompanyId()).userId(accountDto.getId())
                .internalId(request.getInternalId()).build();

        try {
            this.directoryRepository.save(directory);
        } catch (Exception ex) {
            String errMsg = "Couldn't create entry";
            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        LogEntry auditLog = LogEntry.builder().currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz()).targetType("directory").targetId(directoryEntryDto.getUserId())
                .companyId(request.getCompanyId()).teamId("").updatedContents(directoryEntryDto.toString()).build();

        LOGGER.info("updated directory", auditLog);

        OnboardWorkerRequest onboardWorkerRequest = OnboardWorkerRequest.builder().companyId(request.getCompanyId())
                .userId(directoryEntryDto.getUserId()).build();

        this.serviceHelper.onboardWorkerAsync(onboardWorkerRequest);

        this.serviceHelper.trackEventAsync("directoryEntry_created");

        return directoryEntryDto;
    }

    public DirectoryEntryDto updateDirectoryEntry(DirectoryEntryDto request) {
        DirectoryEntryDto orig = this.getDirectoryEntry(request.getCompanyId(), request.getUserId());

        GenericAccountResponse accountResponse;
        try {
            accountResponse = this.accountClient.getAccount(AuthConstant.AUTHORIZATION_COMPANY_SERVICE,
                    request.getUserId());
        } catch (Exception ex) {
            String errMsg = "getting account failed";
            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        if (!accountResponse.isSuccess()) {
            this.serviceHelper.handleErrorAndThrowException(LOGGER, accountResponse.getMessage());
            throw new ServiceException(accountResponse.getMessage());
        }

        AccountDto accountDto = accountResponse.getAccount();

        boolean accountUpdateRequested = !request.getName().equals(accountDto.getName())
                || !request.getEmail().equals(accountDto.getEmail())
                || !request.getPhoneNumber().equals(accountDto.getPhoneNumber());

        if (accountDto.isConfirmedAndActive() && accountUpdateRequested) {
            throw new ServiceException(ResultCode.PARAM_VALID_ERROR, "This user is active, so they cannot be modified");
        } else if (accountDto.isSupport() && accountUpdateRequested) {
            throw new ServiceException(ResultCode.UNAUTHORIZED, "You cannot change this account");
        }

        if (accountUpdateRequested) {
            accountDto.setName(request.getName());
            accountDto.setEmail(request.getEmail());
            accountDto.setPhoneNumber(request.getPhoneNumber());

            GenericAccountResponse updatedAccount;
            try {
                updatedAccount = this.accountClient.updateAccount(AuthConstant.AUTHORIZATION_COMPANY_SERVICE,
                        accountDto);
            } catch (Exception ex) {
                String errMsg = "view updating account";
                this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
                throw new ServiceException(errMsg, ex);
            }

            if (!updatedAccount.isSuccess()) {
                this.serviceHelper.handleErrorAndThrowException(LOGGER, updatedAccount.getMessage());
                throw new ServiceException(updatedAccount.getMessage());
            }

            this.copyAccountToDirectory(accountDto, request);
        }

        try {
            this.directoryRepository.updateInternalIdByCompanyIdAndUserId(request.getInternalId(),
                    request.getCompanyId(), request.getUserId());
        } catch (Exception ex) {
            String errMsg = "fail to update directory";
            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        LogEntry auditLog = LogEntry.builder().currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz()).targetType("directory").targetId(accountDto.getId())
                .companyId(request.getCompanyId()).teamId("").originalContents(orig.toString())
                .updatedContents(request.toString()).build();

        LOGGER.info("updated directory entry for account", auditLog);

        if (!request.isConfirmedAndActive()
                && (!orig.getPhoneNumber().equals(request.getPhoneNumber()) || ("".equals(request.getPhoneNumber())))
                && !orig.getEmail().equals(request.getEmail())) {
            OnboardWorkerRequest onboardWorkerRequest = OnboardWorkerRequest.builder().companyId(request.getCompanyId())
                    .userId(request.getUserId()).build();

            this.serviceHelper.onboardWorkerAsync(onboardWorkerRequest);
        }

        this.serviceHelper.trackEventAsync("directory_entry_updated");

        return request;
    }

    public DirectoryList listDirectory(String companyId, int offset, int limit) {
        if (limit <= 0) {
            limit = 20;
        }

        DirectoryList directoryList = DirectoryList.builder().limit(limit).offset(offset).build();

        PageRequest pageRequest = PageRequest.of(offset, limit);
        Page<Directory> directoryPage = this.directoryRepository.findByCompanyId(companyId, pageRequest);

        for (Directory directory : directoryPage.getContent()) {
            DirectoryEntryDto directoryEntryDto = DirectoryEntryDto.builder().companyId(companyId)
                    .internalId(directory.getInternalId()).userId(directory.getUserId()).build();

            GenericAccountResponse accountResponse;

            try {
                accountResponse = this.accountClient.getAccount(AuthConstant.AUTHORIZATION_COMPANY_SERVICE,
                        directoryEntryDto.getUserId());
            } catch (Exception ex) {
                String errMsg = "Couldn't get account";
                this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
                throw new ServiceException(errMsg, ex);
            }

            this.copyAccountToDirectory(accountResponse.getAccount(), directoryEntryDto);

            directoryList.getAccounts().add(directoryEntryDto);
        }

        return directoryList;
    }

    public DirectoryEntryDto getDirectoryEntry(String companyId, String userId) {
        DirectoryEntryDto directoryEntryDto = DirectoryEntryDto.builder().userId(userId).companyId(companyId).build();

        Directory directory = this.directoryRepository.findByCompanyIdAndUserId(companyId, userId);

        if (Objects.isNull(directory)) {
            throw new ServiceException(ResultCode.NOT_FOUND, "Directory entry not found for user in this company");
        }

        directoryEntryDto.setInternalId(directory.getInternalId());

        GenericAccountResponse accountResponse = null;
        try {
            accountResponse = this.accountClient.getAccount(AuthConstant.AUTHORIZATION_COMPANY_SERVICE, userId);
        } catch (Exception ex) {
            String errMsg = "getting account failed";
            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        if (!accountResponse.isSuccess()) {
            this.serviceHelper.handleErrorAndThrowException(LOGGER, accountResponse.getMessage());
            throw new ServiceException(accountResponse.getMessage());
        }

        this.copyAccountToDirectory(accountResponse.getAccount(), directoryEntryDto);

        return directoryEntryDto;
    }

    public AssociationList getAssociations(String companyId, int offset, int limit) {
        // this handles permissions
        DirectoryList directoryList = this.listDirectory(companyId, offset, limit);

        AssociationList associationList = AssociationList.builder().offset(offset).limit(limit).build();

        for (DirectoryEntryDto directoryEntryDto : directoryList.getAccounts()) {
            Association association = Association.builder().account(directoryEntryDto).build();
            WorkerOfList workerOfList = this.workerService.getWorkerOf(directoryEntryDto.getUserId());

            for (TeamDto teamDto:workerOfList.getTeams()) {
                if (teamDto.getCompanyId().equals(companyId)) {
                    association.getTeams().add(teamDto);
                }

                DirectoryEntryDto admin = this.adminService.getAdmin(companyId, directoryEntryDto.getUserId());
                association.setAdmin(Objects.nonNull(admin));
            }

            associationList.getAccounts().add(association);
        }

        return associationList;
    }

    private void copyAccountToDirectory(AccountDto account, DirectoryEntryDto directoryEntry) {
        directoryEntry.setUserId(account.getId());
        directoryEntry.setName(account.getName());
        directoryEntry.setConfirmedAndActive(account.isConfirmedAndActive());
        directoryEntry.setPhoneNumber(account.getPhoneNumber());
        directoryEntry.setPhotoUrl(account.getPhotoUrl());
        directoryEntry.setEmail(account.getEmail());
    }
}
