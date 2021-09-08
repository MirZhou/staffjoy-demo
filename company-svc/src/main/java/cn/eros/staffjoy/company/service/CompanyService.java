package cn.eros.staffjoy.company.service;

import cn.eros.staffjoy.common.api.ResultCode;
import cn.eros.staffjoy.common.auditlog.LogEntry;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.dto.CompanyDto;
import cn.eros.staffjoy.company.dto.CompanyList;
import cn.eros.staffjoy.company.model.Company;
import cn.eros.staffjoy.company.repo.CompanyRepository;
import cn.eros.staffjoy.company.service.helper.ServiceHelper;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 周光兵
 * @date 2021/8/26 22:39
 */
@Service
public class CompanyService {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(CompanyService.class);

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ServiceHelper serviceHelper;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private EntityManager entityManager;

    public CompanyDto createCompany(CompanyDto companyDto) {
        Company company = this.convertToModel(companyDto);

        Company savedCompany;

        try {
            savedCompany = this.companyRepository.save(company);
        } catch (Exception ex) {
            String errMsg = "Couldn't create company";
            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        LogEntry auditLog = LogEntry.builder()
            .currentUserId(AuthContext.getUserId())
            .authorization(AuthContext.getAuthz())
            .targetType("company")
            .targetId(company.getId())
            .companyId(company.getId())
            .teamId("")
            .updatedContents(company.toString())
            .build();

        LOGGER.info("created company", auditLog);

        this.serviceHelper.trackEventAsync("company_created");

        return this.convertToDto(savedCompany);
    }

    public CompanyDto updateCompany(CompanyDto companyDto) {
        Optional<Company> existingCompany = this.companyRepository.findById(companyDto.getId());
        if (!existingCompany.isPresent()) {
            throw new ServiceException(ResultCode.NOT_FOUND, "Company not found");
        }

        this.entityManager.detach(existingCompany.get());

        Company companyToUpdate = this.convertToModel(companyDto);
        Company updatedCompany;

        try {
            updatedCompany = this.companyRepository.save(companyToUpdate);
        } catch (Exception ex) {
            String errMsg = "Couldn't update the companyDto";
            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        LogEntry auditLog = LogEntry.builder()
            .currentUserId(AuthContext.getUserId())
            .authorization(AuthContext.getAuthz())
            .targetType("company")
            .targetId(companyToUpdate.getId())
            .companyId(companyToUpdate.getId())
            .teamId("")
            .originalContents(existingCompany.get().toString())
            .updatedContents(updatedCompany.toString())
            .build();

        LOGGER.info("updated company", auditLog);

        this.serviceHelper.trackEventAsync("company_updated");

        return this.convertToDto(updatedCompany);
    }

    public CompanyList listCompanies(int offset, int limit) {
        if (limit <= 0) {
            limit = 20;
        }

        Pageable pageRequest = PageRequest.of(offset, limit);
        Page<Company> companyPage;

        try {
            companyPage = this.companyRepository.findAll(pageRequest);
        } catch (Exception ex) {
            String errMsg = "Fail to query database for company list";
            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        List<CompanyDto> companies = companyPage.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        return CompanyList.builder()
            .offset(offset)
            .limit(limit)
            .companies(companies)
            .build();
    }

    public CompanyDto getCompany(String companyId) {
        Optional<Company> optional = this.companyRepository.findById(companyId);

        if (!optional.isPresent()) {
            throw new ServiceException(ResultCode.NOT_FOUND, "Company not found");
        }

        return this.convertToDto(optional.get());
    }

    private CompanyDto convertToDto(Company company) {
        return this.modelMapper.map(company, CompanyDto.class);
    }

    private Company convertToModel(CompanyDto companyDto) {
        return this.modelMapper.map(companyDto, Company.class);
    }
}
