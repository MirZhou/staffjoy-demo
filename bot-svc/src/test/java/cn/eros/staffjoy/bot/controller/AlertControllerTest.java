package cn.eros.staffjoy.bot.controller;

import cn.eros.staffjoy.account.client.AccountClient;
import cn.eros.staffjoy.account.dto.AccountDto;
import cn.eros.staffjoy.account.dto.GenericAccountResponse;
import cn.eros.staffjoy.bot.client.BotClient;
import cn.eros.staffjoy.bot.dto.AlertNewShiftRequest;
import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.company.client.CompanyClient;
import cn.eros.staffjoy.company.dto.*;
import cn.eros.staffjoy.mail.client.MailClient;
import cn.eros.staffjoy.mail.dto.EmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author 周光兵
 * @date 2022/01/07 13:30
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext
@EnableFeignClients(basePackages = {"cn.eros.staffjoy.bot.client"})
@Slf4j
public class AlertControllerTest {
    @Autowired
    private BotClient botClient;

    @MockBean
    private AccountClient accountClient;

    @MockBean
    private CompanyClient companyClient;

    @MockBean
    private MailClient mailClient;

    private String userId;
    private String companyId;
    private String teamId;
    private String jobId;
    private AccountDto accountDto;
    private CompanyDto companyDto;
    private JobDto jobDto;

    @Before
    public void setUp() {
        // arrange mock
        this.userId = UUID.randomUUID().toString();
        this.accountDto = AccountDto.builder()
                .name("test_user001")
                .phoneNumber("18012344321")
                .email("test_user001@staffjoy.xyz")
                .id(this.userId)
                .memberSince(Instant.now().minus(30, ChronoUnit.DAYS))
                .confirmedAndActive(true)
                .photoUrl("http://staffjoy.xyz/photo/test01.png")
                .build();

        when(this.accountClient.getAccount(AuthConstant.AUTHORIZATION_BOT_SERVICE, userId))
                .thenReturn(new GenericAccountResponse(accountDto));

        this.companyId = UUID.randomUUID().toString();
        this.companyDto = CompanyDto.builder()
                .name("test_company001")
                .defaultTimezone(TimeZone.getDefault().getID())
                .defaultDayWeekStarts("Monday")
                .id(this.companyId)
                .build();

        when(this.companyClient.getCompany(AuthConstant.AUTHORIZATION_BOT_SERVICE, this.companyId))
                .thenReturn(new GenericCompanyResponse(companyDto));

        this.teamId = UUID.randomUUID().toString();
        this.jobId = UUID.randomUUID().toString();
        this.jobDto = JobDto.builder()
                .id(this.jobId)
                .companyId(this.companyId)
                .color("#48B7AB")
                .teamId(this.teamId)
                .name("test_job001")
                .build();
        when(this.companyClient.getJob(AuthConstant.AUTHORIZATION_BOT_SERVICE, this.jobId, this.companyId, this.teamId))
                .thenReturn(new GenericJobResponse(this.jobDto));

        TeamDto teamDto = TeamDto.builder()
                .id(this.teamId)
                .name("test_team001")
                .companyId(this.companyId)
                .color("#48B7AB")
                .dayWeekStarts("Monday")
                .timezone(TimeZone.getDefault().getID())
                .build();
        when(this.companyClient.getTeam(AuthConstant.AUTHORIZATION_BOT_SERVICE, this.companyId, this.teamId))
                .thenReturn(new GenericTeamResponse(teamDto));

        when(this.mailClient.send(any(EmailRequest.class)))
                .thenReturn(BaseResponse.builder().message("mail sent").build());
    }

    @Test
    public void testNewShiftAlert() {
        ShiftDto shiftDto = ShiftDto.builder()
                .id(UUID.randomUUID().toString())
                .companyId(this.companyId)
                .teamId(this.teamId)
                .jobId(this.jobId)
                .published(true)
                .userId(this.userId)
                .start(Instant.now().plus(2, ChronoUnit.DAYS))
                .stop(Instant.now().plus(5, ChronoUnit.DAYS))
                .build();
        AlertNewShiftRequest alertNewShiftRequest = AlertNewShiftRequest.builder()
                .userId(this.userId)
                .newShift(shiftDto)
                .build();

        // new shift alert
        BaseResponse baseResponse = this.botClient.alertNewShift(alertNewShiftRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();
    }

}
