package com.z.mai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.log4j.Logger;

public class SendMail {
	static Logger logger = Logger.getLogger(SendMail.class);

	private static Map<String, String> mapFromInfo = new HashMap<String, String>();
	private String mail_title;
	private String mail_content;
	private File[] mail_annex;

	private static List<String> listTo = new ArrayList<String>();
	private static List<String> listCopy = new ArrayList<String>();

	private MimeMessage mimeMessage;
	private Session session;
	private Properties props;

	private void readIni() {
		mapFromInfo.clear();
		mapFromInfo = readToMap("mail_from.ini");
		logger.info("mail_from.ini:\r\n" + mapFromInfo.toString());

		listTo.clear();
		listTo = readToList("mail_to.ini");
		logger.info("mail_to.ini:\r\n" + listTo.toString());

		listCopy.clear();
		listCopy = readToList("mail_copy.ini");
		logger.info("mail_copy.ini:\r\n" + listTo.toString());
	}

	private void readMailTxt() {
		mail_title = "";
		mail_content = "";

		FileReader fr = null;
		try {
			fr = new FileReader("./mail.txt");
		} catch (FileNotFoundException e) {
		}
		BufferedReader br = new BufferedReader(fr);
		String s = null;
		try {
			s = br.readLine();
			mail_title = s;
			while (null != (s = br.readLine())) {
				mail_content += s;
			}
		} catch (IOException e) {
		}
	}

	private void sendMail() {
		String from = mapFromInfo.get("Address");
		String fUserName = mapFromInfo.get("User");
		String fPassWord = mapFromInfo.get("Password");
		String[] to = listToArray(listTo);
		String[] copy = listToArray(listCopy);

		this.mail_annex = null;
		logger.info("��ʼ�����ʼ�...");
		try {
			send(from, fUserName, fPassWord, to, copy, mail_title, mail_content, mail_annex);
		} catch (Exception e) {
		}
		logger.info("�ʼ����ͳɹ�...");
	}

	private static Map<String, String> readToMap(String file) {
		Map<String, String> map = new HashMap<String, String>();
		FileReader fr = null;
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) {
		}
		BufferedReader br = new BufferedReader(fr);
		String s = null;
		while (true) {
			try {
				s = br.readLine();
			} catch (IOException e) {
			}
			if (s == null || s.trim().length() == 0)
				break;
			if (!s.contains(":"))
				continue;
			String key = s.substring(0, s.indexOf(":")).trim();
			if (key == null)
				continue;
			s = s.substring(s.indexOf(":") + 1);
			if (s == null)
				continue;
			if (s.contains("#")) {
				s = s.substring(0, s.indexOf("#")).trim();
			} else {
				s = s.trim();
			}
			if (s == null || s.length() == 0)
				continue;
			map.put(key, s);
		}
		return map;
	}

	private static List<String> readToList(String file) {
		List<String> list = new ArrayList<String>();
		FileReader fr = null;
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) {
		}
		BufferedReader br = new BufferedReader(fr);
		String s = null;
		while (true) {
			try {
				s = br.readLine();
			} catch (IOException e) {
			}
			if (s == null || s.trim().length() == 0)
				break;
			if (s.contains("#")) {
				s = s.substring(0, s.indexOf("#")).trim();
			} else {
				s = s.trim();
			}
			if (s.equals("")) {
				continue;
			}
			list.add(s);
		}
		return list;
	}

	private static String[] listToArray(List<String> list) {
		String string = list.toString();
		string = string.replace("[", "").replace("]", "");
		String[] array = string.split(",");
		if (array[0].trim().length() == 0) {
			return null;
		}
		return array;
	}

	private void send(String from, String fUserName, String fPassWord, String[] to, String[] copy, String title,
			String content, File[] annex) throws Exception {
		try {
			if (from == null || from == "") {
				throw new Exception("���������ַΪ�գ�");
			}
			if (fUserName == null) {
				throw new Exception("���������ַ�ʺ�ΪNULL��");
			}
			if (fPassWord == null) {
				throw new Exception("���������ַ����ΪNULL��");
			}
			if (to == null) {
				throw new Exception("�ռ������ַΪNULL��");
			}
			if (title == null) {
				title = "";
			}
			if (content == null) {
				content = "";
			}

			getSessionObject(); // ��ȡ�ʼ��Ự����
			createMimeMessage(); // ����MIME�ʼ�����

			mimeMessage.setFrom(new InternetAddress(from)); // ���÷��ŵ�ַ
			mimeMessage.setSender(new InternetAddress(from)); // ���÷��ŵ�ַ
			mimeMessage.setSubject(title); // ���ñ���
			mimeMessage.setHeader("Content-Transfer-Encoding", "7bit");

			Address[] adress = new Address[to.length];
			for (int i = 0; i < adress.length; ++i) {
				adress[i] = InternetAddress.parse(to[i])[0];
			}
			mimeMessage.setRecipients(Message.RecipientType.TO, adress); // �������ŵ�ַ

			if (copy != null && copy.length != 0) { // �г����˾ͼ��볭����
				Address[] replyto = new Address[copy.length];
				for (int i = 0; i < copy.length; ++i) {
					replyto[i] = InternetAddress.parse(copy[i])[0];
				}
				mimeMessage.addRecipients(Message.RecipientType.CC, replyto); // ���ó��͵�ַ
				mimeMessage.addRecipients(Message.RecipientType.BCC, replyto); // ���ó��͵�ַ
			}
			mimeMessage.setDescription("description");

			if (annex == null || annex.length == 0) { // û�и���
				mimeMessage.setContent(content, "text/html;charset=gbk"); // ������������
			} else { // �и���
				// mimeMessage.setContent(content, "text/html;charset=gbk");
				addAnnex(annex, content); // ���ø�������������
			}
			mimeMessage.saveChanges();

			System.out.println("���ڷ����ʼ�....");
			Session mailSession = Session.getInstance(props, null);
			Transport transport = mailSession.getTransport("smtp");
			transport.connect((String) props.get("mail.smtp.host"), fUserName, fPassWord);

			String[] receive = mergeArray(to, copy);
			Address[] adressreceive = new Address[receive.length];
			for (int i = 0; i < adressreceive.length; ++i) {
				adressreceive[i] = InternetAddress.parse(receive[i])[0];
			}
			transport.sendMessage(mimeMessage, adressreceive);
			transport.close();
			System.out.println("�����ʼ��ɹ���");

		} catch (Exception e) {
			throw e;
		}
	}

	private static <T> T[] mergeArray(T[] array1, T[] array2) {
		List<T> list = new ArrayList<T>();
		if (array1 != null) {
			for (T t : array1) {
				list.add(t);
			}
		}
		if (array2 != null) {
			for (T t : array2) {
				list.add(t);
			}
		}
		return list.toArray(array1);
	}

	private void addAnnex(File[] annex, String content) throws Exception {
		Multipart mp = new MimeMultipart();
		MimeBodyPart mbpC = new MimeBodyPart();
		mbpC.setContent(content, "text/html;charset=gbk");
		mp.addBodyPart(mbpC);
		for (File f : annex) {
			MimeBodyPart mbp = new MimeBodyPart();
			FileDataSource fds = new FileDataSource(f);

			mbp.setFileName(MimeUtility.encodeText(f.getName()));
			// mbp.setFileName(f.getName());

			mbp.setDataHandler(new DataHandler(fds));
			mp.addBodyPart(mbp);
		}
		mimeMessage.setContent(mp);
	}

	private void getSessionObject() throws Exception {
		try {
			props = System.getProperties();
			props.put("mail.smtp.host", mapFromInfo.get("SMTP_Host"));
			props.put("mail.smtp.port", Integer.parseInt(mapFromInfo.get("SMTP_Port")));
			session = Session.getDefaultInstance(props, null);
		} catch (Exception e) {
			System.out.println("��ȡ�ʼ��Ự����ʱ��������" + e);
			throw e;
		}
	}

	private void createMimeMessage() throws Exception {
		try {
			mimeMessage = new MimeMessage(session);

			if (props == null) {
				props = System.getProperties();
			}
			if (mapFromInfo.get("Auth").equalsIgnoreCase("true")) {
				props.put("mail.smtp.auth", "true");
			} else {
				props.put("mail.smtp.auth", "false");
			}
		} catch (Exception e) {
			System.out.println("����MIME�ʼ�����ʧ�ܣ�" + e);
			throw e;
		}
	}

	public static void main(String[] args) {
		try {
			SendMail sm = new SendMail();
			sm.readIni();
			sm.readMailTxt();
			sm.sendMail();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
