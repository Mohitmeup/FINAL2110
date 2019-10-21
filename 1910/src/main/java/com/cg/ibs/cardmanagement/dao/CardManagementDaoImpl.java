package com.cg.ibs.cardmanagement.dao;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;

import org.apache.log4j.Logger;

import com.cg.ibs.cardmanagement.bean.AccountBean;
import com.cg.ibs.cardmanagement.bean.CaseIdBean;
import com.cg.ibs.cardmanagement.bean.CreditCardBean;
import com.cg.ibs.cardmanagement.bean.CreditCardTransaction;
import com.cg.ibs.cardmanagement.bean.CustomerBean;
import com.cg.ibs.cardmanagement.bean.DebitCardBean;
import com.cg.ibs.cardmanagement.bean.DebitCardTransaction;
import com.cg.ibs.cardmanagement.exceptionhandling.IBSException;
import com.cg.ibs.cardmanagement.ui.CardManagementUI;

public class CardManagementDaoImpl implements CustomerDao, BankDao {
	static Logger log = Logger.getLogger(CardManagementUI.class.getName());

	CaseIdBean caseIdObj = new CaseIdBean();
	DebitCardBean bean = new DebitCardBean();
	CreditCardBean bean1 = new CreditCardBean();
	CustomerBean bean2 = new CustomerBean();
	AccountBean bean3 = new AccountBean();

	@Override
	public void newDebitCard(CaseIdBean caseIdObj, BigInteger accountNumber) {

		String sql = SqlQueries.APPLY_NEW_DEBIT_CARD;

		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, caseIdObj.getCaseIdTotal());
			preparedStatement.setDate(2, java.sql.Date.valueOf(caseIdObj.getCaseTimeStamp().toLocalDate()));
			preparedStatement.setString(3, caseIdObj.getStatusOfQuery());
			preparedStatement.setBigDecimal(4, new BigDecimal(caseIdObj.getAccountNumber()));
			preparedStatement.setBigDecimal(5, new BigDecimal(caseIdObj.getUCI()));
			preparedStatement.setString(6, caseIdObj.getDefineQuery());

			preparedStatement.setString(7, caseIdObj.getCustomerReferenceId());
			preparedStatement.executeUpdate();

		} catch (SQLException | IOException e) {
			log.error(Arrays.toString(e.getStackTrace()));

		}

	}

	@Override
	public List<CaseIdBean> viewAllQueries() {

		List<CaseIdBean> query = null;
	
		
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection
						.prepareStatement(SqlQueries.SELECT_DATA_FROM_QUERY_TABLE);) {

			try (ResultSet resultSet1 = preparedStatement.executeQuery()) {
				query = new ArrayList<>();
			
				while (resultSet1.next()) {

					CaseIdBean caseIdObj = new CaseIdBean();
					Timestamp timestamp = resultSet1.getTimestamp("case_timestamp");
					LocalDateTime localDateTime = timestamp.toLocalDateTime();

					caseIdObj.setCaseIdTotal(resultSet1.getString("query_id"));
					caseIdObj.setCaseTimeStamp(localDateTime);					
					caseIdObj.setStatusOfQuery(resultSet1.getString("status_of_query"));
					caseIdObj.setAccountNumber(BigInteger.valueOf(resultSet1.getLong("account_num")));
					caseIdObj.setUCI(BigInteger.valueOf(resultSet1.getLong("UCI")));
					caseIdObj.setDefineQuery(resultSet1.getString("define_query"));
				
					caseIdObj.setCardNumber(BigInteger.valueOf(resultSet1.getLong("card_num")));
					caseIdObj.setCustomerReferenceId(resultSet1.getString("customer_reference_ID"));

					query.add(caseIdObj);
					

				}

			} catch (Exception e) {
				log.error(Arrays.toString(e.getStackTrace()));

			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return query;

	}

	@Override
	public List<DebitCardBean> viewAllDebitCards() {

		List<DebitCardBean> debitCards = new ArrayList();
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection
						.prepareStatement(SqlQueries.SELECT_DATA_FROM_DEBIT_CARD)) {

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					DebitCardBean deb = new DebitCardBean();
					
					deb.setDebitCardNumber(resultSet.getBigDecimal("debit_card_number").toBigInteger());
					deb.setNameOnDebitCard(resultSet.getString("name_on_deb_card"));
					deb.setDebitCvvNum(resultSet.getInt("debit_cvv_num"));
					deb.setDebitDateOfExpiry(resultSet.getDate("debit_expiry_date").toLocalDate());
					deb.setDebitCardType(resultSet.getString("debit_card_type"));

					debitCards.add(deb);

				}

			} catch (Exception e) {
				log.error(Arrays.toString(e.getStackTrace()));

			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return debitCards;

	}

	public List<CreditCardBean> viewAllCreditCards() {
		String sql = SqlQueries.SELECT_DATA_FROM_CREDIT_CARD;
		List<CreditCardBean> creditCards = new ArrayList();
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					CreditCardBean crd = new CreditCardBean();

					crd.setCreditCardNumber(resultSet.getBigDecimal("credit_card_num").toBigInteger());
					crd.setCreditCardStatus(resultSet.getString("credit_card_status"));
					crd.setNameOnCreditCard(resultSet.getString("name_on_cred_card"));
					crd.setCreditCvvNum(resultSet.getInt("credit_cvv_num"));
					crd.setCreditDateOfExpiry(resultSet.getDate("credit_expiry_date").toLocalDate());
					crd.setCreditCardType(resultSet.getString("credit_card_type"));

					creditCards.add(crd);

				}

			} catch (Exception e) {
				log.error(Arrays.toString(e.getStackTrace()));

			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return creditCards;

	}

	@Override
	public List<CreditCardTransaction> getCreditTrans(int days, BigInteger creditCardNumber) {

		List<CreditCardTransaction> creditCardsList = new ArrayList<>();

		String sql1 = SqlQueries.SELECT_DATA_FROM_CREDIT_TRANSACTION;
		CreditCardTransaction credTran = new CreditCardTransaction();
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql1)) {

			LocalDateTime fromDate1 = LocalDateTime.now().minusDays(days);
			LocalDateTime currentDate1 = LocalDateTime.now();
			Timestamp timestamp1 = Timestamp.valueOf(fromDate1);
			Timestamp timestamp2 = Timestamp.valueOf(currentDate1);
			preparedStatement.setTimestamp(1, timestamp1);
			preparedStatement.setTimestamp(2, timestamp2);
			preparedStatement.setBigDecimal(3, new BigDecimal(creditCardNumber));

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {

					credTran.setCreditCardNumber(resultSet.getBigDecimal("Credit_Card_Num").toBigInteger());
					credTran.setAmount(resultSet.getBigDecimal("amount"));
					credTran.setTransactionid(resultSet.getString("credit_Trans_Id"));
					credTran.setDescription(resultSet.getString("description"));
					credTran.setDateOfTran(resultSet.getTimestamp("Date_Of_trans").toLocalDateTime());
					credTran.setUCI(resultSet.getBigDecimal("UCI").toBigInteger());
					creditCardsList.add(credTran);

				}

			} catch (Exception e) {
				
				log.error(Arrays.toString(e.getStackTrace()));
			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}

		return creditCardsList;
	}

	@Override
	public boolean verifyQueryId(String queryId) {
		boolean result = false;

		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.VERIFY_QUERY_ID);) {
			preparedStatement.setString(1, queryId);

			try (ResultSet resultSet = preparedStatement.executeQuery();) {

				if (resultSet.next()) {

					result = true;
				}

			}
		} catch (Exception e) {

			log.error(Arrays.toString(e.getStackTrace()));

		}
	
		return result;
	}

	@Override
	public void setQueryStatus(String queryId, String newStatus) {
		
		
		
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
		PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.SET_QUERY_STATUS);) 
		{
		preparedStatement.setString(1, newStatus);
		preparedStatement.setString(2, queryId);
		System.out.println(newStatus);
		preparedStatement.executeUpdate();
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));}


	}

	@Override
	public boolean actionANDC(String queryId, DebitCardBean bean)  {
		BigDecimal accNum = null;
		boolean result = false;
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
		PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.GET_DETAILS_DEBIT_CARD_NEW);) {
			preparedStatement.setString(1, queryId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					accNum = resultSet.getBigDecimal("account_num");
					
				}
			}
		} catch (SQLException|IOException e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement2 = connection.prepareStatement(SqlQueries.ACTION_DEBIT_CARD_NEW);) {
	
			preparedStatement2.setBigDecimal(1, accNum) ;
			preparedStatement2.setBigDecimal(2, new BigDecimal(bean.getDebitCardNumber()));
			preparedStatement2.setString(3, bean.getDebitCardStatus());
			preparedStatement2.setString(4, bean.getNameOnDebitCard());
			preparedStatement2.setInt(5, bean.getDebitCvvNum());
			preparedStatement2.setInt(6, bean.getDebitCurrentPin());
			preparedStatement2.setDate(7, java.sql.Date.valueOf(bean.getDebitDateOfExpiry()));
			preparedStatement2.setBigDecimal(8, new BigDecimal(bean.getUCI()));
			preparedStatement2.setString(9, bean.getDebitCardType());
			if (preparedStatement2.executeUpdate() > 0) {
				result = true;
			}

		} catch (SQLException|IOException e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return result;
	}


	@Override
	public boolean actionANCC(String queryId, CreditCardBean bean1)  {
		BigInteger uci = null;
		boolean result = false;
		try(Connection connection = ConnectionProvider.getInstance().getConnection();
		PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.GET_DETAILS_CREDIT_CARD_NEW);) {
			preparedStatement.setString(1, queryId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					uci = resultSet.getBigDecimal("UCI").toBigInteger();
				}
			}
		} catch (SQLException | IOException e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement2 = connection.prepareStatement(SqlQueries.ACTION_CREDIT_CARD_NEW);) {
			preparedStatement2.setBigDecimal(1, new BigDecimal(bean1.getCreditCardNumber()));
			preparedStatement2.setString(2, bean1.getCreditCardStatus());
			preparedStatement2.setString(3, bean1.getNameOnCreditCard());
			preparedStatement2.setInt(4, bean1.getCreditCvvNum());
			preparedStatement2.setInt(5, bean1.getCreditCurrentPin());
			preparedStatement2.setDate(6, java.sql.Date.valueOf(bean1.getCreditDateOfExpiry()));
			preparedStatement2.setBigDecimal(7, new BigDecimal(uci));
			preparedStatement2.setString(8, bean1.getCreditCardType());
			preparedStatement2.setInt(9, bean1.getCreditScore());
			preparedStatement2.setBigDecimal(10, bean1.getCreditLimit());
			preparedStatement2.setDouble(11, bean1.getIncome());
			if (preparedStatement2.executeUpdate() > 0) {
				result = true;
			}

		} catch (SQLException|IOException e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return result;
	}
	@Override
	public String getNewName(BigInteger uci) {
		String firstname = "";
		String lastname = "";
		String name = "";
		try(Connection connection = ConnectionProvider.getInstance().getConnection();
		PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.GET_CREDIT_CARD_NAME);) {
			preparedStatement.setBigDecimal(1, new BigDecimal(uci));
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					firstname = resultSet.getString("first_name");
					lastname = resultSet.getString("last_name");
					name = firstname.concat(" ").concat(lastname);
				}
			}
		} catch (SQLException | IOException e){
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return name;
	}

	public BigInteger getNewUCI(String queryId) {
		BigInteger uci = null;
		try(Connection connection = ConnectionProvider.getInstance().getConnection();
		PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.GET_CREDIT_CARD_UCI);) {
			preparedStatement.setString(1, queryId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					uci = resultSet.getBigDecimal("uci").toBigInteger();
				}
			}
		} catch (SQLException | IOException e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return uci;
	}


	@Override
	public String getNewType(String queryId) {
		String type = "";
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
		PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.GET_CARD_TYPE);) {
			preparedStatement.setString(1, queryId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					type = resultSet.getString("define_query");
				}
			}
		} catch (SQLException|IOException e) {
			log.error(Arrays.toString(e.getStackTrace()));
		} 
		return type;
	}

	@Override
	public void actionBlockDC(String queryId, String status) {
		BigInteger debitCardNum = null;
	  
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.GET_DETAILS_CARD_BLOCK)) {
			preparedStatement.setString(1, queryId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					debitCardNum = resultSet.getBigDecimal("card_num").toBigInteger();
				
				}
			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}

		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement2 = connection
						.prepareStatement(SqlQueries.ACTION_DEBIT_CARD_BLOCK)) {
			preparedStatement2.setString(1, "Blocked");
			preparedStatement2.setBigDecimal(2, new BigDecimal(debitCardNum));
			preparedStatement2.executeUpdate();
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}

	}

	@Override
	public void actionBlockCC(String queryId, String status) {
		BigInteger creditCardNum = null;
		//String creditCardStatus = "";
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.GET_DETAILS_CARD_BLOCK)) {
			preparedStatement.setString(1, queryId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					creditCardNum = resultSet.getBigDecimal("card_num").toBigInteger();
					//creditCardStatus = resultSet.getString("define_query");
				}
			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement2 = connection
						.prepareStatement(SqlQueries.ACTION_CREDIT_CARD_BLOCK);) {
			preparedStatement2.setString(1, status);
			preparedStatement2.setBigDecimal(2, new BigDecimal(creditCardNum));
			preparedStatement2.executeUpdate();
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}

	}

	@Override
	public void actionUpgradeDC(String queryId) {
		BigInteger debitCardNum = null;
		String debitCardType = "";

		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection
						.prepareStatement(SqlQueries.GET_DETAILS_CARD_UPGRADE);) {
			preparedStatement.setString(1, queryId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					
					debitCardNum = resultSet.getBigDecimal("card_num").toBigInteger();

					debitCardType = resultSet.getString("define_query");
				}
			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement2 = connection
						.prepareStatement(SqlQueries.ACTION_DEBIT_CARD_UPGRADE)) {
			preparedStatement2.setString(1, debitCardType);
			preparedStatement2.setBigDecimal(2, new BigDecimal(debitCardNum));
			preparedStatement2.executeUpdate();
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}

	}

	@Override
	public void actionUpgradeCC(String queryId) {
		BigInteger creditCardNum = null;
		String creditCardType = "";
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection
						.prepareStatement(SqlQueries.GET_DETAILS_CARD_UPGRADE);) {
			preparedStatement.setString(1, queryId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					creditCardNum = resultSet.getBigDecimal("card_num").toBigInteger();
					creditCardType = resultSet.getString("define_query");
				}
			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement2 = connection
						.prepareStatement(SqlQueries.ACTION_CREDIT_CARD_UPGRADE)) {
			preparedStatement2.setString(1, creditCardType);
			preparedStatement2.setBigDecimal(2, new BigDecimal(creditCardNum));
			preparedStatement2.executeUpdate();
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}

	}

	@Override
	public void newCreditCard(CaseIdBean caseIdObj) {
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.APPLY_NEW_CREDIT_CARD)) {

			preparedStatement.setString(1, caseIdObj.getCaseIdTotal());
			preparedStatement.setDate(2, java.sql.Date.valueOf(LocalDateTime.now().toLocalDate()));
			preparedStatement.setString(3, caseIdObj.getStatusOfQuery());
			preparedStatement.setBigDecimal(4, new BigDecimal(caseIdObj.getUCI()));
			preparedStatement.setString(5, caseIdObj.getDefineQuery());
			preparedStatement.setString(6, caseIdObj.getCustomerReferenceId());
			preparedStatement.executeUpdate();
			

		} catch (SQLException | IOException e) {
			log.error(Arrays.toString(e.getStackTrace()));

		}

	}

	@Override
	public String getdebitCardType(BigInteger debitCardNumber) {
		String sql = SqlQueries.GET_DEBIT_CARD_TYPE;
		String type = new String();
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setBigDecimal(1, new BigDecimal(debitCardNumber));

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					DebitCardBean deb = new DebitCardBean();
					String debitCardType = resultSet.getString("debit_card_type");
					deb.setDebitCardType(debitCardType);

					type = deb.getDebitCardType();
				}

			} catch (Exception e) {
				log.error(Arrays.toString(e.getStackTrace()));

			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return type;
	}

	@Override
	public boolean verifyAccountNumber(BigInteger accountNumber) {
		boolean result = false;

		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection
						.prepareStatement(SqlQueries.VERIFY_ACCOUNT_NUM_FROM_ACCOUNT);) {
			preparedStatement.setBigDecimal(1, new BigDecimal(accountNumber));

			try (ResultSet resultSet = preparedStatement.executeQuery();) {

				if (resultSet.next()) {

					result = true;
				}

			}
		} catch (Exception e) {

			log.error(Arrays.toString(e.getStackTrace()));

		}

		return result;
	}

	@Override
	public boolean verifyDebitCardNumber(BigInteger debitCardNumber) {
		boolean result = false;

		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.VERIFY_DEBIT_CARD_NUM);) {
			preparedStatement.setBigDecimal(1, new BigDecimal(debitCardNumber));

			try (ResultSet resultSet = preparedStatement.executeQuery();) {

				if (resultSet.next()) {

					result = true;
				}

			}
		} catch (Exception e) {

			log.error(Arrays.toString(e.getStackTrace()));

		}

		return result;
	}

	@Override
	public boolean verifyCreditCardNumber(BigInteger creditCardNumber) {
		boolean result = false;

		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.VERIFY_CREDIT_CARD_NUM);) {
			preparedStatement.setBigDecimal(1, new BigDecimal(creditCardNumber));

			try (ResultSet resultSet = preparedStatement.executeQuery();) {

				if (resultSet.next()) {

					result = true;
				}

			}
		} catch (Exception e) {

			log.error(Arrays.toString(e.getStackTrace()));

		}

		return result;
	}

	@Override
	public int getDebitCardPin(BigInteger debitCardNumber) {
		String sql = SqlQueries.GET_DEBIT_CARD_PIN;
		int debitCardPin = 0;
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setBigDecimal(1, new BigDecimal(debitCardNumber));

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					debitCardPin = resultSet.getInt("debit_current_pin");

				}

			} catch (Exception e) {
				log.error(Arrays.toString(e.getStackTrace()));

			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return debitCardPin;
	}

	@Override
	public void setNewCreditPin(BigInteger creditCardNumber, int newPin) {

		String sql = SqlQueries.SET_CREDIT_CARD_PIN;
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql);) {

			preparedStatement.setInt(1, newPin);
			preparedStatement.setBigDecimal(2, new BigDecimal(creditCardNumber));
			preparedStatement.executeUpdate();

		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}

	}

	@Override
	public int getCreditCardPin(BigInteger creditCardNumber) {
		String sql = SqlQueries.GET_CREDIT_CARD_PIN;
		int creditCardPin = 0;
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setBigDecimal(1, new BigDecimal(creditCardNumber));

			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {

				creditCardPin = resultSet.getInt("credit_cur_pin");

			}

		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));

		}

		return creditCardPin;

	}

	@Override
	public BigInteger getDebitUci(BigInteger debitCardNumber) {
		BigInteger debitCardUci = null;
		BigInteger debitCardNum=debitCardNumber;
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.GET_DEBIT_UCI)) {
			
			preparedStatement.setBigDecimal(1, new BigDecimal(debitCardNum));

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					debitCardUci = resultSet.getBigDecimal("UCI").toBigInteger();

				}

			} catch (Exception e) {
				log.error(Arrays.toString(e.getStackTrace()));

			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return debitCardUci;
	}

	@Override
	public BigInteger getCreditUci(BigInteger creditCardNumber) {
		BigInteger creditCardUci = null;
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.GET_CREDIT_UCI)) {
			preparedStatement.setBigDecimal(1, new BigDecimal(creditCardNumber));

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					creditCardUci = resultSet.getBigDecimal("UCI").toBigInteger();

				}

			} catch (Exception e) {
				log.error(Arrays.toString(e.getStackTrace()));

			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return creditCardUci;
	}

	@Override
	public String getcreditCardType(BigInteger creditCardNumber) {

		String sql = SqlQueries.GET_CREDIT_CARD_TYPE;
		String type = new String();
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setBigDecimal(1, new BigDecimal(creditCardNumber));

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					type = resultSet.getString("credit_card_type");

				}

			} catch (Exception e) {
				log.error(Arrays.toString(e.getStackTrace()));

			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return type;
	}

	@Override
	public boolean verifyCreditTransactionId(String transactionId) {
		boolean result = false;

		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.VERIFY_CREDIT_TRANS_ID);) {
			preparedStatement.setString(1, transactionId);

			try (ResultSet resultSet = preparedStatement.executeQuery();) {

				if (resultSet.next()) {

					result = true;
				}

			}
		} catch (Exception e) {

			log.error(Arrays.toString(e.getStackTrace()));

		}

		return result;
	}

	@Override
	public void raiseDebitMismatchTicket(CaseIdBean caseIdObj, String transactionId) {
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection
						.prepareStatement(SqlQueries.REQUEST_DEBIT_MISMATCH_TICKET)) {

			preparedStatement.setString(1, caseIdObj.getCaseIdTotal());
			preparedStatement.setDate(2, java.sql.Date.valueOf(caseIdObj.getCaseTimeStamp().toLocalDate()));
			preparedStatement.setString(3, caseIdObj.getStatusOfQuery());
		
			preparedStatement.setBigDecimal(4, new BigDecimal(caseIdObj.getAccountNumber()));

			preparedStatement.setBigDecimal(5, new BigDecimal(caseIdObj.getUCI()));
			preparedStatement.setString(6, caseIdObj.getDefineQuery());

			preparedStatement.setBigDecimal(7, new BigDecimal(caseIdObj.getCardNumber()));
			preparedStatement.setString(8, caseIdObj.getCustomerReferenceId());
			preparedStatement.executeUpdate();

		} catch (SQLException | IOException e) {
			log.error(Arrays.toString(e.getStackTrace()));

		}

	}

	@Override
	public void raiseCreditMismatchTicket(CaseIdBean caseIdObj, String transactionId) {
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection
						.prepareStatement(SqlQueries.REQUEST_CREDIT_MISMATCH_TICKET)) {

			preparedStatement.setString(1, caseIdObj.getCaseIdTotal());
			preparedStatement.setDate(2, java.sql.Date.valueOf(caseIdObj.getCaseTimeStamp().toLocalDate()));
			preparedStatement.setString(3, caseIdObj.getStatusOfQuery());

			preparedStatement.setBigDecimal(4, new BigDecimal(caseIdObj.getUCI()));
			preparedStatement.setString(5, caseIdObj.getDefineQuery());

			preparedStatement.setBigDecimal(6, new BigDecimal(caseIdObj.getCardNumber()));
			preparedStatement.setString(7, caseIdObj.getCustomerReferenceId());
			preparedStatement.executeUpdate();

		} catch (SQLException | IOException e) {
			log.error(Arrays.toString(e.getStackTrace()));

		}

	}

	@Override
	public List<DebitCardTransaction> getDebitTrans(int days, BigInteger debitCardNumber) {
		List<DebitCardTransaction> debitCardsList = new ArrayList<>();

		String sql1 = SqlQueries.SELECT_DATA_FROM_DEBIT_TRANSACTION;
		DebitCardTransaction debitTran = new DebitCardTransaction();
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql1)) {

			LocalDateTime fromDate1 = LocalDateTime.now().minusDays(days);
			LocalDateTime currentDate1 = LocalDateTime.now();
			Timestamp timestamp1 = Timestamp.valueOf(fromDate1);
			Timestamp timestamp2 = Timestamp.valueOf(currentDate1);
			preparedStatement.setTimestamp(1, timestamp1);
			preparedStatement.setTimestamp(2, timestamp2);
			preparedStatement.setBigDecimal(3, new BigDecimal(debitCardNumber));

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {

					debitTran.setDebitCardNumber(resultSet.getBigDecimal("Debit_Card_Num").toBigInteger());
					debitTran.setAmount(resultSet.getBigDecimal("amount"));
					debitTran.setTransactionid(resultSet.getString("debit_Trans_Id"));
					debitTran.setDescription(resultSet.getString("description"));
					debitTran.setDate(resultSet.getTimestamp("Date_Of_trans").toLocalDateTime());
					debitTran.setUCI(resultSet.getBigDecimal("UCI").toBigInteger());
					debitCardsList.add(debitTran);

				}

			} catch (Exception e) {
				log.error(e.getMessage());
				log.error(Arrays.toString(e.getStackTrace()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return debitCardsList;
	}

	@Override
	public boolean verifyDebitTransactionId(String transactionId) {
		boolean result = false;
		
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.VERIFY_DEBIT_TRANS_ID);) {
			preparedStatement.setString(1, transactionId);

			try (ResultSet resultSet = preparedStatement.executeQuery();) {
			
				if (resultSet.next()) {

					result = true;
				}

			}
		} catch (Exception e) {

			log.error(Arrays.toString(e.getStackTrace()));

		}

		return result;
	}

	@Override
	public String getCustomerReferenceId(CaseIdBean caseIdObj, String customerReferenceId) {

		String custReferenceStatus = null;
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection
						.prepareStatement(SqlQueries.GET_CUSTOMER_REFERENCE_ID)) {
			preparedStatement.setString(1, customerReferenceId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					custReferenceStatus = resultSet.getString("status_of_query");

				}

			} catch (Exception e) {
				log.error(Arrays.toString(e.getStackTrace()));

			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return custReferenceStatus;

	}

	@Override
	public String getDebitCardStatus(BigInteger debitCardNumber) {
		String debitStatus = new String();
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.GET_DEBIT_CARD_STATUS);) {
			preparedStatement.setBigDecimal(1, new BigDecimal(debitCardNumber));

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					debitStatus = resultSet.getString("debit_card_status");
					

				}

			} catch (Exception e) {
				log.error(Arrays.toString(e.getStackTrace()));

			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return debitStatus;
	}

	@Override
	public String getCreditCardStatus(BigInteger creditCardNumber) {
		String creditStatus = new String();
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.GET_CREDIT_CARD_STATUS);) {
			preparedStatement.setBigDecimal(1, new BigDecimal(creditCardNumber));

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					creditStatus = resultSet.getString("credit_card_status");

				}

			} catch (Exception e) {
				log.error(Arrays.toString(e.getStackTrace()));

			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return creditStatus;
	}

	@Override
	public BigInteger getDebitCardNumber(String transactionId) {
		String sql = SqlQueries.GET_DEBIT_CARD_NUMBER;
		BigInteger debitCardNum = null;
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, transactionId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					debitCardNum = resultSet.getBigDecimal("debit_Card_num").toBigInteger();

				}

			} catch (Exception e) {
				log.error(Arrays.toString(e.getStackTrace()));

			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		System.out.println(debitCardNum);
		return debitCardNum;

	}

	@Override
	public BigInteger getDMUci(String transactionId) {

		BigInteger debitCardUci = null;
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.GET_DM_UCI)) {
			preparedStatement.setString(1, transactionId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					debitCardUci = resultSet.getBigDecimal("UCI").toBigInteger();

				}

			} catch (Exception e) {
				System.out.println(e.getMessage());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return debitCardUci;

	}

	@Override
	public BigInteger getDMAccountNumber(BigInteger debitCardNumber) {
		BigInteger acc = null;
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.GET_DM_ACCOUNT_NUMBER);) {
			preparedStatement.setBigDecimal(1, new BigDecimal(debitCardNumber));

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					acc = resultSet.getBigDecimal("account_number").toBigInteger();
					
				}

			} 
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return acc;
	}

	@Override
	public BigInteger getCMUci(String transactionId) {

		BigInteger creditCardUci = null;
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.GET_CM_UCI)) {
			preparedStatement.setString(1, transactionId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					creditCardUci = resultSet.getBigDecimal("UCI").toBigInteger();

				}

			} catch (Exception e) {
				System.out.println(e.getMessage());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return creditCardUci;
	}

	@Override
	public BigInteger getNDCUci(BigInteger accountNumber) {

		String sql = SqlQueries.GET_NDC_UCI;
		BigInteger ndcUci = null;
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setBigDecimal(1, new BigDecimal(accountNumber));

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					ndcUci = resultSet.getBigDecimal("UCI").toBigInteger();

				}

			} catch (Exception e) {
				log.error(Arrays.toString(e.getStackTrace()));

			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return ndcUci;
	}

	@Override
	public void requestCreditCardLost(CaseIdBean caseIdObj, BigInteger creditCardNumber) {

		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection
						.prepareStatement(SqlQueries.REQUEST_CREDIT_CARD_LOST);) {

			preparedStatement.setString(1, caseIdObj.getCaseIdTotal());
			preparedStatement.setDate(2, java.sql.Date.valueOf(caseIdObj.getCaseTimeStamp().toLocalDate()));
			preparedStatement.setString(3, caseIdObj.getStatusOfQuery());

			preparedStatement.setBigDecimal(4, new BigDecimal(caseIdObj.getUCI()));
			preparedStatement.setString(5, caseIdObj.getDefineQuery());

			preparedStatement.setBigDecimal(6, new BigDecimal(caseIdObj.getCardNumber()));
			preparedStatement.setString(7, caseIdObj.getCustomerReferenceId());
			preparedStatement.executeUpdate();

		} catch (SQLException | IOException e) {
			log.error(Arrays.toString(e.getStackTrace()));

		}

	}

	@Override
	public void requestDebitCardLost(CaseIdBean caseIdObj, BigInteger debitCardNumber) {
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.REQUEST_DEBIT_CARD_LOST);) {

			preparedStatement.setString(1, caseIdObj.getCaseIdTotal());
			preparedStatement.setDate(2, java.sql.Date.valueOf(caseIdObj.getCaseTimeStamp().toLocalDate()));
			
			preparedStatement.setString(3, caseIdObj.getStatusOfQuery());
			preparedStatement.setBigDecimal(4,new BigDecimal(caseIdObj.getAccountNumber()));

			preparedStatement.setBigDecimal(5, new BigDecimal(caseIdObj.getUCI()));
			preparedStatement.setString(6, caseIdObj.getDefineQuery());

			preparedStatement.setBigDecimal(7, new BigDecimal(caseIdObj.getCardNumber()));
			preparedStatement.setString(8, caseIdObj.getCustomerReferenceId());
			
			preparedStatement.executeUpdate();
			preparedStatement.close();

		} catch (SQLException | IOException e) {
			log.error(Arrays.toString(e.getStackTrace()));

		}

	}

	@Override
	public void requestDebitCardUpgrade(CaseIdBean caseIdObj, BigInteger debitCardNumber) {
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection
						.prepareStatement(SqlQueries.REQUEST_DEBIT_CARD_UPGRADE)) {
			
			preparedStatement.setString(1, caseIdObj.getCaseIdTotal());
			preparedStatement.setDate(2, java.sql.Date.valueOf(caseIdObj.getCaseTimeStamp().toLocalDate()));
		
			preparedStatement.setString(3, caseIdObj.getStatusOfQuery());
			preparedStatement.setBigDecimal(4, new BigDecimal(caseIdObj.getAccountNumber()));
			preparedStatement.setBigDecimal(5, new BigDecimal(caseIdObj.getUCI()));
			
			preparedStatement.setString(6, caseIdObj.getDefineQuery());

			preparedStatement.setBigDecimal(7, new BigDecimal(caseIdObj.getCardNumber()));
			preparedStatement.setString(8, caseIdObj.getCustomerReferenceId());
			int count = preparedStatement.executeUpdate();
		

		} catch (SQLException | IOException e) {
			log.error(Arrays.toString(e.getStackTrace()));

		}

	}

	@Override
	public void requestCreditCardUpgrade(CaseIdBean caseIdObj, BigInteger creditCardNumber) {

		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection
						.prepareStatement(SqlQueries.REQUEST_CREDIT_CARD_UPGRADE)) {

			preparedStatement.setString(1, caseIdObj.getCaseIdTotal());
			preparedStatement.setDate(2, java.sql.Date.valueOf(caseIdObj.getCaseTimeStamp().toLocalDate()));
			preparedStatement.setString(3, caseIdObj.getStatusOfQuery());

			preparedStatement.setBigDecimal(4, new BigDecimal(caseIdObj.getUCI()));
			preparedStatement.setString(5, caseIdObj.getDefineQuery());

			preparedStatement.setBigDecimal(6, new BigDecimal(caseIdObj.getCardNumber()));
			preparedStatement.setString(7, caseIdObj.getCustomerReferenceId());
			preparedStatement.executeUpdate();

		} catch (SQLException | IOException e) {
			log.error(Arrays.toString(e.getStackTrace()));

		}

	}

	@Override
	public BigInteger getAccountNumber(BigInteger debitCardNumber) {

		BigInteger accountNumber = null;
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.GET_ACCOUNT_NUMBER)) {
			preparedStatement.setBigDecimal(1, new BigDecimal(debitCardNumber));

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					accountNumber = resultSet.getBigDecimal("account_number").toBigInteger();

				}

			} catch (Exception e) {
				log.error(Arrays.toString(e.getStackTrace()));

			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return accountNumber;

	}

	@Override
	public void setNewDebitPin(BigInteger debitCardNumber, int newPin) {
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.SET_DEBIT_PIN)) {
			preparedStatement.setInt(1, newPin);
			preparedStatement.setBigDecimal(2, new BigDecimal(debitCardNumber));
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));

		}

	}

	@Override
	public BigInteger getCreditCardNumber(String transactionId) {
		String sql = SqlQueries.GET_CREDIT_CARD_NUMBER;
		BigInteger creditCardNum = null;
		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, transactionId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					creditCardNum = resultSet.getBigDecimal("credit_Card_num").toBigInteger();

				}

			} catch (Exception e) {
				log.error(Arrays.toString(e.getStackTrace()));

			}
		} catch (Exception e) {
			log.error(Arrays.toString(e.getStackTrace()));
		}
		return creditCardNum;

	}

	@Override
	public boolean verifyUCI(BigInteger uci) {
		boolean result = false;

		try (Connection connection = ConnectionProvider.getInstance().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.VERIFY_UCI);) {
			preparedStatement.setBigDecimal(1, new BigDecimal(uci));

			try (ResultSet resultSet = preparedStatement.executeQuery();) {

				if (resultSet.next()) {
					result = true;
				}
			}
		} catch (Exception e) {

			log.error(Arrays.toString(e.getStackTrace()));
		}

		return result;
	}
}
