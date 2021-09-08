package cn.eros.staffjoy.company.service;

import cn.eros.staffjoy.common.auditlog.LogEntry;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.dto.AdminEntries;
import cn.eros.staffjoy.company.dto.AdminOfList;
import cn.eros.staffjoy.company.dto.CompanyDto;
import cn.eros.staffjoy.company.dto.DirectoryEntryDto;
import cn.eros.staffjoy.company.model.Admin;
import cn.eros.staffjoy.company.repo.AdminRepository;
import cn.eros.staffjoy.company.service.helper.ServiceHelper;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author 周光兵
 * @date 2021/8/26 22:36
 */
@Service
public class AdminService {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(AdminService.class);

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private DirectoryService directoryService;

    @Autowired
    private ServiceHelper serviceHelper;

    public AdminEntries listAdmins(String companyId) {
        this.companyService.getCompany(companyId);

        AdminEntries adminEntries = AdminEntries.builder()
            .companyId(companyId)
            .build();

        List<Admin> admins = this.adminRepository.findByCompanyId(companyId);

        for (Admin admin : admins) {
            DirectoryEntryDto directoryEntry = this.directoryService.getDirectoryEntry(companyId, admin.getUserId());
            adminEntries.getAdmins().add(directoryEntry);
        }

        return adminEntries;
    }

    public DirectoryEntryDto getAdmin(String companyId, String userId) {
        return this.directoryService.getDirectoryEntry(companyId, userId);
    }

    public DirectoryEntryDto createAdmin(String companyId, String userId) {
        Admin existing = this.adminRepository.findByCompanyIdAndUserId(companyId, userId);

        if (Objects.nonNull(existing)) {
            throw new ServiceException("User is already an admin");
        }

        DirectoryEntryDto directoryEntryDto = this.directoryService.getDirectoryEntry(companyId, userId);

        try {
            Admin admin = Admin.builder()
                .companyId(companyId)
                .userId(userId)
                .build();

            this.adminRepository.save(admin);
        } catch (Exception ex) {
            String errMsg = "Could not create the admin";
            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        LogEntry logEntry = LogEntry.builder()
            .currentUserId(AuthContext.getUserId())
            .authorization(AuthContext.getAuthz())
            .targetType("admin")
            .targetId(userId)
            .companyId(companyId)
            .teamId("")
            .build();

        LOGGER.info("added admin", logEntry);

        this.serviceHelper.trackEventAsync("admin_created");

        return directoryEntryDto;
    }

    public void deleteAdmin(String companyId, String userId) {
        this.getAdmin(companyId, userId);

        try {
            this.adminRepository.deleteByCompanyIdAndUserId(companyId, userId);
        } catch (Exception ex) {
            String errMsg = "could not delete the admin";
            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        LogEntry auditLog = LogEntry.builder()
            .currentUserId(AuthContext.getUserId())
            .authorization(AuthContext.getAuthz())
            .targetType("admin")
            .targetId(userId)
            .companyId(companyId)
            .teamId("")
            .build();

        LOGGER.info("removed admin", auditLog);

        this.serviceHelper.trackEventAsync("admin_deleted");
    }

    public AdminOfList getAdminOf(String userId) {
        AdminOfList adminOfList = AdminOfList.builder()
            .userid(userId)
            .build();

        List<Admin> admins = this.adminRepository.findByUserId(userId);

        for (Admin admin : admins) {
            CompanyDto companyDto = this.companyService.getCompany(admin.getCompanyId());
            adminOfList.getCompanies().add(companyDto);
        }

        return adminOfList;
    }
}
