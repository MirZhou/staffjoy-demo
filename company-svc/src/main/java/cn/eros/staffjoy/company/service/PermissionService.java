package cn.eros.staffjoy.company.service;

import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.auth.PermissionDeniedException;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.model.Admin;
import cn.eros.staffjoy.company.repo.AdminRepository;
import cn.eros.staffjoy.company.service.helper.ServiceHelper;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author 周光兵
 * @date 2021/8/26 22:02
 */
@Service
public class PermissionService {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(PermissionService.class);

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ServiceHelper serviceHelper;

    public void checkPermissionCompanyAdmin(String companyId) {
        String currentUserId = this.checkAndGetCurrentUserId();

        Admin admin;
        try {
            admin = this.adminRepository.findByCompanyIdAndUserId(companyId, currentUserId);
        } catch (Exception ex) {
            String errMsg = "failed to check company admin permissions";
            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        if (admin == null) {
            throw new PermissionDeniedException("You don't have admin access to this service.");
        }
    }

    private String checkAndGetCurrentUserId() {
        String currentUserId = AuthContext.getUserId();

        if (StringUtils.isEmpty(currentUserId)) {
            String errMsg = "failed to find current user id";

            this.serviceHelper.handleErrorAndThrowException(LOGGER, errMsg);
            throw new ServiceException(errMsg);
        }

        return currentUserId;
    }
}
