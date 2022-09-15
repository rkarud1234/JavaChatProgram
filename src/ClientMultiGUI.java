
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

public class ClientMultiGUI extends JFrame implements ActionListener {
	private JTextField tf;
	private JTextField tfName;
	private JTextField tfServer, tfPort;
	private JButton connect;
	private JButton send;
	private JTextArea ta;
	private JTextArea list;
	private boolean connected;
	private PrintWriter writer;

	ClientMultiGUI(String host, int port) {
		super("Chat Client");

		JPanel northPanel = new JPanel(new GridLayout(3, 1));
		JPanel serverAndPort = new JPanel(new GridLayout(1, 5, 1, 3));
		tfServer = new JTextField(host);
		tfPort = new JTextField("" + port);
		serverAndPort.add(new JLabel("Server Address:  "));
		serverAndPort.add(tfServer);
		serverAndPort.add(new JLabel("Port Number:  "));
		serverAndPort.add(tfPort);
		serverAndPort.add(new JLabel("")); // 공간마련
		northPanel.add(serverAndPort);

		JPanel userAndConnect = new JPanel(new GridLayout(1, 5, 1, 3));
		connect = new JButton("Connect");
		send = new JButton("전송");
		connect.addActionListener(this);
		send.addActionListener(this);
		userAndConnect.add(new JLabel("Name"));
		tfName = new JTextField("김이박");
		userAndConnect.add(tfName);
		userAndConnect.add(connect);
		userAndConnect.add(send);
		userAndConnect.add(new JLabel(""));
		northPanel.add(userAndConnect);
		tf = new JTextField("보낼 메시지");
		tf.setBackground(Color.WHITE);
		northPanel.add(tf);
		add(northPanel, BorderLayout.NORTH);

		ta = new JTextArea("접속 대기중. . .\n", 50, 40);
		ta.setEditable(false);
		JPanel centerPanel = new JPanel(new GridLayout(2, 1));
		centerPanel.add(new JScrollPane(ta));

		list = new JTextArea("참여자 목록", 10, 40);
		list.setEditable(false);

		centerPanel.add(new JScrollPane(list));
		add(centerPanel, BorderLayout.CENTER);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
		tf.requestFocus();

	}

	void append(String str) {
		ta.append(str);
		ta.setCaretPosition(ta.getText().length() - 1);
	}

	void userList(String users) {
		list.setText(users);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (connected) { // connected가 true이고 이벤트가 있다면 text 입력 이벤트임
			writer.println(tf.getText());
			writer.flush();
			tf.setText("");
			return;
		}

		if (o == connect) {
			String username = tfName.getText().trim();
			if (username.length() == 0)
				return;
			String server = tfServer.getText().trim();
			if (server.length() == 0)
				return;
			String portNumber = tfPort.getText().trim();
			if (portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			} catch (Exception en) {
				return; // 포트번호가 없으면 할 일이 없음
			}

			Socket sc = null;
			try {
				sc = new Socket(server, port);
				writer = new PrintWriter(sc.getOutputStream());
				// 내이름부터 서버로 보내준 이후에. 화면입력을 보내는 방식. 일종의 로그인 절차
				writer.println(username);
				writer.flush();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			if (sc == null) {
				return;
			}

			Thread receiver = new ReceiverThreadM(sc, this);
			ta.append("채팅방에 오신 걸 환영합니다!!\n");
			receiver.start();
			connected = true;
			connect.setEnabled(false);
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			tfName.setEditable(false);
			// 메시지 입력가능하도록
			tf.addActionListener(this);
		}
	}

	public static void main(String[] args) {
		new ClientMultiGUI("127.0.0.1", 2222);
	}
}

// 소켓으로부터 데이터를 읽어오는 쓰레드
class ReceiverThreadM extends Thread {
	Socket socket;
	ClientMultiGUI cg;

	ReceiverThreadM(Socket socket, ClientMultiGUI cg) {
		this.socket = socket;
		this.cg = cg;
	}

	public void run() {
		try {
			// 소켓으로부터 입력스트림을 준비하고 read가 편하게 하기위해
			// BufferedReader를 사용하겠음
			// 한줄씩 읽어오는 메소드를 활용하기 위함

			// 소켓으로부터 입력스트림을 가져오고
			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isr);

			while (true) {
				// reader.readLine() 부분에서 데이터를 수신할때까지 대기
				String str = reader.readLine();
				if (str == null)
					break;
				if (str.equals("!user")) {
					StringBuilder sb = new StringBuilder("참여자 목록\n");
					while (reader.ready()) {
						sb.append(reader.readLine()).append("\n");
					}
					cg.userList(sb.toString());
				} else {
					cg.append(str + "\n");
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}