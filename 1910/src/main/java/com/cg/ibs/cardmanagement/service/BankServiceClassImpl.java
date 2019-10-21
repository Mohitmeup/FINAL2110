package com.cg.ibs.cardmanagement.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cg.ibs.cardmanagement.bean.CaseIdBean;
import com.cg.ibs.cardmanagement.bean.CreditCardBean;
import com.cg.ibs.cardmanagement.bean.CreditCardTransaction;
import com.cg.ibs.cardmanagement.bean.DebitCardBean;
import com.cg.ibs.cardmanagement.bean.DebitCardTransaction;
import com.cg.ibs.cardmanagement.dao.BankDao;
import com.cg.ibs.cardmanagement.dao.CardManagementDaoImpl;
import com.cg.ibs.cardmanagement.exceptionhandling.ErrorMessages;
import com.cg.ibs.cardmanagement.exceptionhandling.IBSException;

public class BankServiceClassImpl implements BankService {

	BankDao bankDao = new CardManagementDaoImpl();
	CaseIdBean caseIdObj = new CaseIdBean();
	DebitCardBean bean = new DebitCardBean();
	CreditCardBean bean1 = new CreditCardBean();
	Random random = new Random();

	@Override
	public List<CaseIdBean> viewQueries() {

		

		return bankDao.viewAllQueries();

	}

	public List<DebitCardTransaction> getDebitTransactions(int dys, BigInteger debitCardNumber) throws IBSException {

		List<DebitCardTransaction> debitCardBeanTrns = bankDao.getDebitTrans(dys, debitCardNumber);
		if (debitCardBeanTrns.isEmpty())
			throw new IBSException("NO TRANSACTIONS");
		return bankDao.getDebitTrans(dys, debitCardNumber);
	}

	@Override
	public boolean verifyQueryId(String queryId) throws IBSException {

		boolean check = bankDao.verifyQueryId(queryId);
		if (check) {
			return true;
		} else {

			throw new IBSException("Invalid Query Id");
		}
	}

	public boolean verifyDebitCardNumber(BigInteger debitCardNumber) throws IBSException {
		String debitCardNum = debitCardNumber.toString();
		Pattern pattern = Pattern.compile("[0-9]{16}");
		Matcher matcher = pattern.matcher(debitCardNum);
		if (!(matcher.find() && matcher.group().equals(debitCardNum)))
			throw new IBSException("Incorrect  length");
		boolean check = bankDao.verifyDebitCardNumber(debitCardNumber);
		if (!check)
			throw new IBSException(" Debit Card Number does not exist");
		return (check);

	}

	public boolean verifyCreditCardNumber(BigInteger creditCardNumber) throws IBSException {
		String creditCardNum = creditCardNumber.toString();
		Pattern pattern = Pattern.compile("[0-9]{16}");
		Matcher matcher = pattern.matcher(creditCardNum);
		if (!(matcher.find() && matcher.group().equals(creditCardNum)))
			throw new IBSException("Incorrect  length");
		boolean check1 = bankDao.verifyCreditCardNumber(creditCardNumber);
		if (!check1)
			throw new IBSException(" Credit Card Number does not exist");
		return (check1);
	}

	@Override
	public List<CreditCardTransaction> getCreditTrans(int days, BigInteger creditCardNumber) throws IBSException {

		List<CreditCardTransaction> creditCardBeanTrns = bankDao.getCreditTrans(days, creditCardNumber);
		if (creditCardBeanTrns.isEmpty())
			throw new IBSException("NO TRANSACTIONS");
		return bankDao.getCreditTrans(days, creditCardNumber);

	}
	public String getNewQueryStatus(int newQueryStatus) throws IBSException {
		String queryStatus = newQueryStatus + "";
		Pattern pattern = Pattern.compile("[123]");
		Matcher matcher = pattern.matcher(queryStatus);
			if (!(matcher.find() && matcher.group().equals(queryStatus)))
		throw new IBSException("Not a valid input");

		switch (newQueryStatus) {
		case 1:

		queryStatus = "Approved";
		break;
		case 2:
		queryStatus = "In Process";
		break;
		case 3:
		queryStatus = "Disapproved";
		break;
		default: queryStatus="Pending";
		break;

		}
		return queryStatus;
		}

	public void checkDays(int days1) throws IBSException {
		if (days1 < 1) {

			throw new IBSException("Statement can not be generated for less than 1 day");

		} else if (days1 >= 730) {

			throw new IBSException("Enter days less than 730");
		}

	}

	@Override
	public void setQueryStatus(String queryId, String newStatus)  {
		bankDao.setQueryStatus(queryId, newStatus);
		if (newStatus.contains("Approved")) {
			if (queryId.contains("ANDC")) {
				getNewDC(queryId);
			} else if (queryId.contains("ANCC")) {
				getNewCC(queryId);
			} else if (queryId.contains("RDCL")) {
				System.out.println("dfd");
				blockDC(queryId);
			} else if (queryId.contains("RCCL")) {
				blockCC(queryId);
			} else if (queryId.contains("RDCU")) {
				
				upgradeDC(queryId);
			} else if (queryId.contains("RCCU")) {
				upgradeCC(queryId);
			}
			
		}
	}

	private void upgradeCC(String queryId) {
		bankDao.actionUpgradeCC(queryId);
	}

	private void upgradeDC(String queryId) {
	
		bankDao.actionUpgradeDC(queryId);
	}

	private void blockCC(String queryId) {
		String status = "Blocked";
		bankDao.actionBlockCC(queryId, status);
	}

	private void blockDC(String queryId) {
		String status = "Blocked";
		bankDao.actionBlockDC(queryId, status);
	}

	private boolean getNewCC(String queryId) {
		boolean result = false;
		BigInteger uci = bankDao.getNewUCI(queryId);
		bean1.setUCI(uci);
		String Name = bankDao.getNewName(uci);
		bean1.setNameOnCreditCard(Name);
		String cvvString = String.format("%04d", random.nextInt(1000));
		int cvv = Integer.parseInt(cvvString);
		bean1.setCreditCvvNum(cvv);
		String pinString = String.format("%04d", random.nextInt(10000));
		int pin = Integer.parseInt(pinString);
		bean1.setCreditCurrentPin(pin);
		Long first14 = (long) (Math.random() * 100000000000000L);
		Long number = 5200000000000000L + first14;
		BigInteger creditCardNumber = BigInteger.valueOf(number);
		bean1.setCreditCardNumber(creditCardNumber);
		String scoreString = String.format("%04d", random.nextInt(1000));
		int score = Integer.parseInt(scoreString);
		bean1.setCreditScore(score);
		String incomeString = String.format("%04d", random.nextInt(100000));
		double income = Integer.parseInt(incomeString);
		bean1.setIncome(income);
		String status = "Active";
		bean1.setCreditCardStatus(status);
		LocalDate expiry = LocalDate.now().plusYears(5);
		bean1.setCreditDateOfExpiry(expiry);
		String type = bankDao.getNewType(queryId);
		if (type.equals("Platinum")) {
			bean1.setCreditCardType("Platinum");
			bean1.setCreditLimit(new BigDecimal(500000));
		} else if (type.equals("Gold")) {
			bean1.setCreditCardType("Gold");
			bean1.setCreditLimit(new BigDecimal(100000));
		}else if (type.equals("Silver")) {
			bean1.setCreditCardType("Silver");
			bean1.setCreditLimit(new BigDecimal(50000));
		}

		if (bankDao.actionANCC(queryId, bean1)) {
			result = true;
		}
		return result;

	}


	public boolean getNewDC(String queryId)  {
		boolean result = false;
		BigInteger uci = bankDao.getNewUCI(queryId);
		bean.setUCI(uci);
		String Name = bankDao.getNewName(uci);
		bean.setNameOnDebitCard(Name);
		String cvvString = String.format("%04d", random.nextInt(1000));
		int cvv = Integer.parseInt(cvvString);
		bean.setDebitCvvNum(cvv);
		String pinString = String.format("%04d", random.nextInt(10000));
		int pin = Integer.parseInt(pinString);
		bean.setDebitCurrentPin(pin);
		Long first14 = (long) (Math.random() * 100000000000000L);
		Long number = 5200000000000000L + first14;
		BigInteger debitCardNumber = BigInteger.valueOf(number);
		bean.setDebitCardNumber(debitCardNumber);
		String status = "Active";
		bean.setDebitCardStatus(status);
		LocalDate expiry = LocalDate.now().plusYears(5);
		bean.setDebitDateOfExpiry(expiry);
		String type = bankDao.getNewType(queryId);
		System.out.println("aaaaaaaaaaaaaa" +queryId);
		if (type.equals("Platinum")) {
			bean.setDebitCardType("Platinum");
		} else if (type.equals("Gold")) {
			bean.setDebitCardType("Gold");
		} else if (type.equals("Silver")) {
			bean.setDebitCardType("Silver");
		}
		if (bankDao.actionANDC(queryId, bean)) {
			result = true;
		}
		return result;
		
	}
	
}