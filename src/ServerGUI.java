
import java.awt.*;
import java.net.*;

import javax.swing.*;

public class ServerGUI extends JFrame {
	private JTextArea chat, event;
	private JTextField tPortNumber, tIP;
	private ServerMulti server;

	ServerGUI(int port) {
		super("Chat Server"); // 제목
		server = null;

		// Port num, IP Address 입력받는 부분
		JPanel north = new JPanel();
		north.add(new JLabel("Port number: "));
		tPortNumber = new JTextField("  " + port);
		tPortNumber.setEditable(false);
		north.add(tPortNumber);
		north.add(new JLabel("IP Address: "));
		String addr = "";
		try {
			addr = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		tIP = new JTextField(addr);
		tIP.setEditable(false);
		north.add(tIP);
		add(north, BorderLayout.NORTH);
		// *********************//

		chat = new JTextArea(10, 20);
		chat.setEditable(false);
		appendRoom("채팅방이 열렸습니다.(IP: " + addr + ", port: " + port + ")\n");
		add(new JScrollPane(chat));
		setSize(400, 600);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		startServer();
	}

	private void startServer() {
		int port;
		try {
			port = Integer.parseInt(tPortNumber.getText().trim());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		// ceate a new Server
		server = new ServerMulti(port, this);
		// and start it as a thread
	}

	void appendRoom(String str) {
		chat.append(str);
		chat.setCaretPosition(chat.getText().length() - 1); // 스크롤 될때 마지막 위치 표시
	}

	public static void main(String[] arg) {
		new ServerGUI(2222);
	}
}