import Data.DataHelper;
import Page.DashboardPage;
import Page.LoginPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferMoneyTest {
    @BeforeAll
    public static void loginToPersonalAccount() {
        open("http://localhost:9999/");
        var loginPage = new LoginPage();
        var authInfo = DataHelper.getUserAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = DataHelper.getVerificationCodeFor();
        verificationPage.validVerify(verificationCode);
    }

    @AfterEach
    public void cardBalancing() {
        var dashboardPage = new DashboardPage();
        var firstCardId = DataHelper.getFirstCardId();
        var balanceFirstCard = dashboardPage.getCardBalance(firstCardId);
        var secondCardId = DataHelper.getSecondCardId();
        var balanceSecondCard = dashboardPage.getCardBalance(secondCardId);
        int amountTransfer;
        if (balanceFirstCard > balanceSecondCard) {
            amountTransfer = (balanceFirstCard - balanceSecondCard) / 2;
            var replenishmentPage = dashboardPage.transfer(secondCardId);
            var transferInfo = DataHelper.setSecondCardTransferInfo(amountTransfer);
            replenishmentPage.transferBetweenOwnCards(transferInfo);
        }
        if (balanceFirstCard < balanceSecondCard) {
            amountTransfer = (balanceSecondCard - balanceFirstCard) / 2;
            var replenishmentPage = dashboardPage.transfer(firstCardId);
            var transferInfo = DataHelper.setFirstCardTransferInfo(amountTransfer);
            replenishmentPage.transferBetweenOwnCards(transferInfo);
        }
    }


    @Test
    @DisplayName("Transfer money from the second card to the first card")
    public void shouldTransferFromSecondToFirst() {
        var dashboardPage = new DashboardPage();
        var firstCardId = DataHelper.getFirstCardId();
        var initialBalanceFirstCard = dashboardPage.getCardBalance(firstCardId);
        var secondCardId = DataHelper.getSecondCardId();
        var initialBalanceSecondCard = dashboardPage.getCardBalance(secondCardId);
        var replenishmentPage = dashboardPage.transfer(firstCardId);
        var transferInfo = DataHelper.getFirstCardTransferInfoPositive();
        replenishmentPage.transferBetweenOwnCards(transferInfo);
        var finalBalanceFirstCard = dashboardPage.getCardBalance(firstCardId);
        var finalBalanceSecondCard = dashboardPage.getCardBalance(secondCardId);
        assertEquals(transferInfo.getAmount(), finalBalanceFirstCard - initialBalanceFirstCard);
        //Проверка списания со второй карты:
        assertEquals(transferInfo.getAmount(), initialBalanceSecondCard - finalBalanceSecondCard);
    }

    @Test
    @DisplayName("Transfer money from the first card to the second card")
    public void shouldTransferFromFirstToSecond() {
        var dashboardPage = new DashboardPage();
        var firstCardId = DataHelper.getFirstCardId();
        var initialBalanceFirstCard = dashboardPage.getCardBalance(firstCardId);
        var secondCardId = DataHelper.getSecondCardId();
        var initialBalanceSecondCard = dashboardPage.getCardBalance(secondCardId);
        var replenishmentPage = dashboardPage.transfer(secondCardId);
        var transferInfo = DataHelper.getSecondCardTransferInfoPositive();
        //Осуществление перевода денег:
        replenishmentPage.transferBetweenOwnCards(transferInfo);
        //Получение итогового баланса по обеим картам:
        var finalBalanceFirstCard = dashboardPage.getCardBalance(firstCardId);
        var finalBalanceSecondCard = dashboardPage.getCardBalance(secondCardId);
        //Проверка списания с первой карты:
        assertEquals(transferInfo.getAmount(), initialBalanceFirstCard - finalBalanceFirstCard);
        //Проверка зачисления на вторую карту:
        assertEquals(transferInfo.getAmount(), finalBalanceSecondCard - initialBalanceSecondCard);
    }

    //Негативные проверки:
    @Test
    @DisplayName("Transferring money from the second card to the first card with a negative amount")
    public void shouldTransferFromSecondToFirstNegativeAmount() {
        var dashboardPage = new DashboardPage();
        var firstCardId = DataHelper.getFirstCardId();
        var initialBalanceFirstCard = dashboardPage.getCardBalance(firstCardId);
        var secondCardId = DataHelper.getSecondCardId();
        var initialBalanceSecondCard = dashboardPage.getCardBalance(secondCardId);
        var replenishmentPage = dashboardPage.transfer(firstCardId);
        var transferInfo = DataHelper.getFirstCardTransferInfoNegative();
        replenishmentPage.transferBetweenOwnCards(transferInfo);
        var finalBalanceFirstCard = dashboardPage.getCardBalance(firstCardId);
        var finalBalanceSecondCard = dashboardPage.getCardBalance(secondCardId);
        assertEquals(-transferInfo.getAmount(), finalBalanceFirstCard - initialBalanceFirstCard);
        //Проверка списания со второй карты:
        assertEquals(-transferInfo.getAmount(), initialBalanceSecondCard - finalBalanceSecondCard);
    }

    @Test
    @DisplayName("Transfer money from the first card to the second " +
            "with the transfer amount exceeding the balance of the first card")
    public void shouldTransferFromFirstToSecondNegativeAmount() throws InterruptedException {
        //Получение баланса по обеим картам и подготовка данных для перевода денег:
        var dashboardPage = new DashboardPage();
        var firstCardId = DataHelper.getFirstCardId();
        var initialBalanceFirstCard = dashboardPage.getCardBalance(firstCardId);
        var secondCardId = DataHelper.getSecondCardId();
        var initialBalanceSecondCard = dashboardPage.getCardBalance(secondCardId);
        var replenishmentPage = dashboardPage.transfer(secondCardId);
        var transferInfo = DataHelper.getSecondCardTransferInfoNegative();
        //Попытка осуществление перевода денег:
        replenishmentPage.transferBetweenOwnCards(transferInfo);
        //Получение итогового баланса по обеим картам:
        var finalBalanceFirstCard = dashboardPage.getCardBalance(firstCardId);
        var finalBalanceSecondCard = dashboardPage.getCardBalance(secondCardId);
        //Проверка на изменение баланса первой карты:
        assertEquals(initialBalanceFirstCard, finalBalanceFirstCard,
                "Изменился баланс первой карты");
        //Проверка на изменение баланса второй карты:
        assertEquals(initialBalanceSecondCard, finalBalanceSecondCard,
                "Изменился баланс второй карты");
    }
}
