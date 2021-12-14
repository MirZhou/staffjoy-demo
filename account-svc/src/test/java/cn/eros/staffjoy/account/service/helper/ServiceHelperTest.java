package cn.eros.staffjoy.account.service.helper;

import cn.eros.staffjoy.account.model.Account;
import cn.eros.staffjoy.account.repo.AccountRepo;
import cn.eros.staffjoy.common.env.EnvConfig;
import cn.eros.staffjoy.company.client.CompanyClient;
import cn.eros.staffjoy.company.dto.*;
import io.intercom.api.CustomAttribute;
import io.intercom.api.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author 周光兵
 * @date 2021/12/1 22:39
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ServiceHelperTest {
    @Mock
    private AccountRepo accountRepo;

    @Mock
    private CompanyClient companyClient;

    @Mock
    private EnvConfig envConfig;

    @InjectMocks
    @Spy
    private ServiceHelper serviceHelper;

    @Test
    public void testSyncUserAsync() {
        Account account = Account.builder()
            .id(UUID.randomUUID().toString())
            .name("test_user")
            .email("test_user@jskillcloud.com")
            .phoneNumber("1111112321423")
            .confirmedAndActive(true)
            .memberSince(Instant.now())
            .photoUrl("http://test/test.png")
            .build();

        when(this.envConfig.isDebug()).thenReturn(false);
        when(this.accountRepo.findById(anyString())).thenReturn(Optional.of(account));
        doNothing().when(this.serviceHelper).syncUserWithIntercom(any(User.class), eq(account.getId()));

        String companyId = UUID.randomUUID().toString();
        WorkerOfList workerOfList = WorkerOfList.builder()
            .userid(account.getId())
            .build();
        workerOfList.getTeams().add(TeamDto.builder()
            .name("test_team")
            .companyId(companyId)
            .build());
        when(this.companyClient.getWorkerOf(anyString(), eq(account.getId())))
            .thenReturn(new GetWorkerOfResponse(workerOfList));

        CompanyDto companyDto = CompanyDto.builder()
            .id(companyId)
            .name("test_company")
            .build();
        when(this.companyClient.getCompany(anyString(), eq(companyId)))
            .thenReturn(new GenericCompanyResponse(companyDto));

        AdminOfList adminOfList = AdminOfList.builder()
            .userid(account.getId())
            .build();
        adminOfList.getCompanies().add(companyDto);
        when(this.companyClient.getAdminOf(anyString(), eq(account.getId())))
            .thenReturn(new GetAdminOfResponse(adminOfList));

        this.serviceHelper.syncUserAsync(account.getId());

        ArgumentCaptor<User> argUser = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<String> argAccountId = ArgumentCaptor.forClass(String.class);

        verify(this.serviceHelper, times(1))
            .syncUserWithIntercom(argUser.capture(), argAccountId.capture());
        User user = argUser.getValue();
        String accountId = argAccountId.getValue();

        log.info(user.toString());
        log.info(accountId);

        assertThat(user.getUserId()).isEqualTo(account.getId());
        assertThat(user.getEmail()).isEqualTo(account.getEmail());
        assertThat(user.getName()).isEqualTo(account.getName());
        assertThat(user.getSignedUpAt()).isEqualTo(account.getMemberSince().toEpochMilli());
        assertThat(user.getAvatar().getImageURL().toString()).isEqualTo(account.getPhotoUrl());
        assertThat(user.getCustomAttributes().get("v2"))
            .isEqualTo(CustomAttribute.newBooleanAttribute("v2", true));
        assertThat(user.getCustomAttributes().get("phone_number"))
            .isEqualTo(CustomAttribute.newStringAttribute("phone_number", account.getPhoneNumber()));
        assertThat(user.getCustomAttributes().get("confirmed_and_active"))
            .isEqualTo(CustomAttribute.newBooleanAttribute("confirmed_and_active", account.isConfirmedAndActive()));
        assertThat(user.getCustomAttributes().get("is_worker"))
            .isEqualTo(CustomAttribute.newBooleanAttribute("is_worker", true));
        assertThat(user.getCustomAttributes().get("is_admin"))
            .isEqualTo(CustomAttribute.newBooleanAttribute("is_admin", true));
        assertThat(user.getCustomAttributes().get("is_staffjoy_account"))
            .isEqualTo(CustomAttribute.newBooleanAttribute("is_staffjoy_account", account.isSupport()));

        io.intercom.api.Company company = user.getCompanyCollection().next();
        assertThat(company.getCompanyID()).isEqualTo(companyId);
        assertThat(company.getName()).isEqualTo(companyDto.getName());
    }

    @Test
    public void testIsAlmostSameInstant() {
        Instant now = Instant.now();
        Instant twoSecondsLater = now.plusSeconds(2L);

        assertThat(this.serviceHelper.isAlmostSameInstant(now, twoSecondsLater)).isFalse();

        Instant oneSecondLater = now.plusSeconds(1L);

        assertThat(this.serviceHelper.isAlmostSameInstant(now, oneSecondLater)).isFalse();

        Instant haveSecondLater = now.plus(500000, ChronoUnit.MICROS);
        assertThat(this.serviceHelper.isAlmostSameInstant(now, haveSecondLater)).isTrue();
    }
}
