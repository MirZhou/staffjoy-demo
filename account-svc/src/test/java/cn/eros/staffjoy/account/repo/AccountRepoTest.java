package cn.eros.staffjoy.account.repo;

import cn.eros.staffjoy.account.model.Account;
import cn.eros.staffjoy.account.model.AccountSecret;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author 周光兵
 * @date 2021/11/25 22:16
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RunWith(SpringRunner.class)
public class AccountRepoTest {
    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private AccountSecretRepo accountSecretRepo;

    private Account newAccount;

    @Before
    public void setUp() {
        newAccount = Account.builder()
            .name("testAccount")
            .email("test@staffjoy.net")
            .memberSince(LocalDateTime.of(2019, 1, 20, 12, 50).atZone(ZoneId.systemDefault()).toInstant())
            .confirmedAndActive(false)
            .photoUrl("https://staffjoy.xyz/photo/test.png")
            .phoneNumber("18012344321")
            .support(false)
            .build();

        // sanity check
        this.accountRepo.deleteAll();
    }

    @Test//(expected = DuplicateKeyException.class)
    public void createSampleAccount() {
        this.accountRepo.save(newAccount);

        assertTrue(this.accountRepo.existsById(newAccount.getId()));
    }

    @Test
    public void getAccountById() {
        this.accountRepo.save(newAccount);
        assertEquals(1, this.accountRepo.count());

        Optional<Account> foundAccount = this.accountRepo.findById(newAccount.getId());
        assertTrue(foundAccount.isPresent());
        assertEquals(newAccount, foundAccount.get());
    }

    @Test
    public void findAccountByEmail() {
        // not existing
        Account foundAccount = this.accountRepo.findAccountByEmail("notexisting@staffjoy.net");
        assertNull(foundAccount);

        this.accountRepo.save(newAccount);
        assertEquals(1, this.accountRepo.count());
        foundAccount = this.accountRepo.findAccountByEmail(newAccount.getEmail());
        assertEquals(newAccount.getId(), foundAccount.getId());
    }

    @Test
    public void findAccountByPhoneNumber() {
        // not existing
        Account foundAccount = this.accountRepo.findAccountByPhoneNumber("18080000008");
        assertNull(foundAccount);

        this.accountRepo.save(newAccount);
        assertEquals(1, this.accountRepo.count());
        foundAccount = this.accountRepo.findAccountByPhoneNumber(newAccount.getPhoneNumber());
        assertEquals(newAccount.getId(), foundAccount.getId());
    }

    @Test
    public void listAccount() {
        Pageable pageRequest = PageRequest.of(0, 2);
        // test empty
        Page<Account> accounts = this.accountRepo.findAll(pageRequest);
        assertEquals(0, accounts.getTotalElements());

        // create 1 new
        this.accountRepo.save(newAccount);
        assertEquals(1, this.accountRepo.count());

        // create 2 more
        newAccount.setId(null);
        this.accountRepo.save(newAccount);
        assertEquals(2, this.accountRepo.count());
        newAccount.setId(null);
        this.accountRepo.save(newAccount);
        assertEquals(3, this.accountRepo.count());
        accounts = this.accountRepo.findAll(pageRequest);
        assertEquals(2, accounts.getNumberOfElements());

        pageRequest = pageRequest.next();
        accounts = this.accountRepo.findAll(pageRequest);
        assertEquals(1, accounts.getNumberOfElements());
        assertEquals(2, accounts.getTotalPages());
        assertEquals(3, accounts.getTotalElements());
    }

    @Test
    public void updateAccount() {
        // create new
        this.accountRepo.save(newAccount);
        assertEquals(1, this.accountRepo.count());

        Account toUpdateAccount = newAccount;
        toUpdateAccount.setName("update");
        toUpdateAccount.setEmail("update@staffjoy.xyz");
        Account updatedAccount = this.accountRepo.save(toUpdateAccount);
        Optional<Account> foundAccount = this.accountRepo.findById(updatedAccount.getId());
        assertTrue(foundAccount.isPresent());
        assertEquals(updatedAccount, foundAccount.get());

        toUpdateAccount.setConfirmedAndActive(true);
        toUpdateAccount.setSupport(true);
        toUpdateAccount.setPhoneNumber("19012344321");
        toUpdateAccount.setPhotoUrl("http://staffjoy.net/photo/update.png");
        updatedAccount = this.accountRepo.save(toUpdateAccount);
        foundAccount = this.accountRepo.findById(updatedAccount.getId());
        assertTrue(foundAccount.isPresent());
        assertEquals(updatedAccount, foundAccount.get());
    }

    @Test
    public void updateEmailAndActivateById() {
        // create new
        Account account = this.accountRepo.save(newAccount);
        assertEquals(1, this.accountRepo.count());
        assertFalse(account.isConfirmedAndActive());

        String toUpdateEmail = "update@staffjoy.xyz";
        int result = this.accountRepo.updateEmailAndActivateById(toUpdateEmail, newAccount.getId());
        assertEquals(1, result);

        Account updatedAccount = this.accountRepo.findAccountByEmail(toUpdateEmail);
        assertEquals(toUpdateEmail, updatedAccount.getEmail());
        assertTrue(updatedAccount.isConfirmedAndActive());
    }

    @Test
    public void updatePasswordById() {
        // create new
        Account account = this.accountRepo.save(newAccount);
        assertEquals(1, this.accountRepo.count());
        assertFalse(account.isConfirmedAndActive());

        String passwordHash = "testHash";
        int result = this.accountSecretRepo.updatePasswordHashById(passwordHash, newAccount.getId());
        assertEquals(1, result);

        AccountSecret foundAccountSecret = this.accountSecretRepo.findAccountSecretByEmail(newAccount.getEmail());
        assertNotNull(foundAccountSecret);
        assertEquals(newAccount.getId(), foundAccountSecret.getId());
        assertEquals(newAccount.isConfirmedAndActive(), foundAccountSecret.isConfirmedAndActive());
        assertEquals(passwordHash, foundAccountSecret.getPasswordHash());
    }

    @After
    public void destroy() {
        this.accountRepo.deleteAll();
    }
}
