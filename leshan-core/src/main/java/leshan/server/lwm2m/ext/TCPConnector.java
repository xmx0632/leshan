package leshan.server.lwm2m.ext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.RawData;
import org.eclipse.californium.elements.RawDataChannel;

/**
 * The TCPConnector connects a server to the network using the TCP protocol. The
 * <code>TCPConnector</code> is bound to an {@link Endpoint} by a
 * {@link RawDataChannel}. An <code>Endpoint</code> sends messages encapsulated
 * within a {@link RawData} by calling the method {@link #send(RawData)} on the
 * connector. When the connector receives a message, it invokes
 * {@link RawDataChannel#receiveData(RawData)}. // TODO: describe that we can
 * make many threads
 */
public class TCPConnector implements Connector {

	public final static Logger LOGGER = Logger.getLogger(TCPConnector.class.toString());

	public static final int UNDEFINED = 0;

	private boolean running;

	// private DatagramSocket socket;

	private ServerSocket tcpServerSocket;

	private final InetSocketAddress localAddr;

	private List<Thread> receiverThreads;
	private List<Thread> senderThreads;

	/** The queue of outgoing block (for sending). */
	private final BlockingQueue<RawData> outgoing; // Messages to send

	/** The receiver of incoming messages */
	private RawDataChannel receiver; // Receiver of messages

	private int receiveBuffer = UNDEFINED;
	private int sendBuffer = UNDEFINED;

	private int senderCount = 1;
	private int receiverCount = 1;

	private int receiverPacketSize = 2048;
	private boolean logPackets = false;

	public TCPConnector() {
		this(new InetSocketAddress(0));
	}

	public TCPConnector(InetSocketAddress address) {
		this.localAddr = address;
		this.running = false;

		this.outgoing = new LinkedBlockingQueue<RawData>();
	}

	@Override
	public synchronized void start() throws IOException {
		if (running)
			return;

		// if localAddr is null or port is 0, the system decides
		tcpServerSocket = new ServerSocket(localAddr.getPort());
		this.running = true;
		while (true) {
			System.out.println("start licensing on " + localAddr.getPort());
			Socket connectionSocket = tcpServerSocket.accept();
			System.out.println("new client connecting");

			try {
				Receiver receiver = new Receiver("TCP-Receiver " + localAddr, connectionSocket);
				Sender sender = new Sender("TCP-Sender " + localAddr, connectionSocket);
				receiver.start();
				sender.start();

				receiver.join();
				sender.join();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (connectionSocket != null) {
					try {
						connectionSocket.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	@Override
	public synchronized void stop() {
		if (!running)
			return;
		this.running = false;
		// stop all threads
		if (senderThreads != null)
			for (Thread t : senderThreads)
				t.interrupt();
		if (receiverThreads != null)
			for (Thread t : receiverThreads)
				t.interrupt();
		outgoing.clear();

		try {
			if (tcpServerSocket != null) {
				tcpServerSocket.close();
			}
		} catch (IOException e) {
		}
		tcpServerSocket = null;
	}

	@Override
	public synchronized void destroy() {
		stop();
	}

	@Override
	public void send(RawData msg) {
		if (msg == null)
			throw new NullPointerException();
		outgoing.add(msg);
	}

	@Override
	public void setRawDataReceiver(RawDataChannel receiver) {
		this.receiver = receiver;
	}

	public InetSocketAddress getAddress() {
		if (tcpServerSocket == null)
			return localAddr;
		else
			return new InetSocketAddress(tcpServerSocket.getInetAddress(), tcpServerSocket.getLocalPort());
	}

	private abstract class Worker extends Thread {

		/**
		 * Instantiates a new worker.
		 *
		 * @param name
		 *            the name
		 */
		private Worker(String name) {
			super(name);
			setDaemon(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			LOGGER.config("Start " + getName());
//			while (running) {
				try {
					work();
				} catch (Throwable t) {
					if (running)
						LOGGER.log(Level.WARNING, "Exception \"" + t + "\" in thread " + getName() + ": running=" + running, t);
					else
						LOGGER.info(getName() + " has successfully stopped");
				}
//			}
		}

		/**
		 * @throws Exception
		 *             the exception to be properly logged
		 */
		protected abstract void work() throws Exception;
	}

	private class Receiver extends Worker {
		private Socket clientConnection;

		private Receiver(String name, Socket clientConnection) {
			super(name);
			this.clientConnection = clientConnection;
		}

		protected void work() throws IOException {

			byte[] bytes = input2byte(clientConnection.getInputStream());
//			byte[] bytes = udpBytes();
			printBytes(bytes);
			RawData msg = new RawData(bytes);
			InetAddress inetAddress = clientConnection.getInetAddress();
			LOGGER.info("inetAddress:" + inetAddress);
			msg.setAddress(inetAddress);
			int port = clientConnection.getPort();
			System.out.println("ppp:" + port);
			msg.setPort(port);
			// String message = new String(bytes);
			// LOGGER.info("msg:" + message);

			receiver.receiveData(msg);
		}

		private void printBytes(byte[] bytes) {
			String ss = Util.bytesToHex(bytes);
			LOGGER.info("bytes==" + ss);
		}

		private byte[] udpBytes() {
			// TODO Auto-generated method stub
			String hexStringUdp = "4002DDB436646F6D61696E82726411283665703D6E7463076C743D333630300C65743D506F7765724E6F646508643D646F6D61696EFF3C2F6E772F706970616464723E3B63743D2230223B72743D226E733A763661646472223B69663D22636F72652373222C3C2F6465762F6D66673E3B63743D2230223B72743D226970736F3A6465762D6D6667223B69663D22222C3C2F7077722F302F72656C3E3B6F62733B63743D2230223B72743D226970736F3A7077722D72656C223B69663D22222C3C2F6465762F6D646C3E3B63743D2230223B72743D226970736F3A6465762D6D646C223B69663D22222C3C2F6465762F6261743E3B6F62733B63743D2230223B72743D226970736F3A6465762D626174223B69663D22222C3C2F7077722F302F773E3B6F62733B63743D2230223B72743D226970736F3A7077722D77223B69663D22222C3C2F6E772F6970616464723E3B63743D2230223B72743D226E733A763661646472223B69663D22636F72652373222C3C2F73656E2F74656D703E3B6F62733B63743D2230223B72743D227563756D3A43656C223B69663D22222C3C2F6E772F65726970616464723E3B63743D2230223B72743D226E733A763661646472223B69663D22636F72652373222C3C2F6E772F70727373693E3B63743D2230223B72743D226E733A72737369223B69663D22636F7265237322";
			byte[] bytes = Util.hexStringToBytes(hexStringUdp);
			return bytes;
		}

		public byte[] input2byte(InputStream inStream) {
			try {
				int count = 0;
				while (count == 0) {
					count = inStream.available();
				}
				byte[] b = new byte[count];
				inStream.read(b);
//				 inStream.close();
				return b;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	private class Sender extends Worker {

		private Socket clientConnection;

		private Sender(String name, Socket clientConnection) {
			super(name);
			this.clientConnection = clientConnection;
		}

		protected void work() throws InterruptedException, IOException {
			RawData raw = outgoing.take(); // Blocking
			byte[] bytes = raw.getBytes();
			OutputStream outputStream = clientConnection.getOutputStream();
			outputStream.write(bytes);

			if (outputStream != null) {
				try {
					LOGGER.info("close outputStream");
					outputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (clientConnection != null) {
				try {
					LOGGER.info("close clientConnection");
					// clientConnection.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public void setReceiveBufferSize(int size) {
		this.receiveBuffer = size;
	}

	public int getReceiveBufferSize() {
		return receiveBuffer;
	}

	public void setSendBufferSize(int size) {
		this.sendBuffer = size;
	}

	public int getSendBufferSize() {
		return sendBuffer;
	}

	public void setReceiverThreadCount(int count) {
		this.receiverCount = count;
	}

	public int getReceiverThreadCount() {
		return receiverCount;
	}

	public void setSenderThreadCount(int count) {
		this.senderCount = count;
	}

	public int getSenderThreadCount() {
		return senderCount;
	}

	public void setReceiverPacketSize(int size) {
		this.receiverPacketSize = size;
	}

	public int getReceiverPacketSize() {
		return receiverPacketSize;
	}

	public void setLogPackets(boolean b) {
		this.logPackets = b;
	}

	public boolean isLogPackets() {
		return logPackets;
	}
}