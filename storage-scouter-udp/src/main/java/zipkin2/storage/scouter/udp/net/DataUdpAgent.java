/*
 *  Copyright 2015 the original author or authors. 
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 */
package zipkin2.storage.scouter.udp.net;

import scouter.io.DataInputX;
import scouter.io.DataOutputX;
import scouter.net.NetCafe;
import scouter.util.KeyGen;
import zipkin2.storage.scouter.udp.ScouterConfig;
import zipkin2.storage.scouter.udp.ScouterUDPStorage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataUdpAgent {
	private static final Logger logger = Logger.getLogger(DataUdpAgent.class.getName());
	private static DataUdpAgent inst;

	InetAddress serverHost;
	int serverPort;
	private DatagramSocket datagram;

	private DataUdpAgent() {
		setTarget();
		openDatagramSocket();
	}

	private void setTarget() {
		ScouterConfig config = ScouterUDPStorage.getConfig();
		if (config != null) {
			String host = config.getAddress();
			int port = config.getPort();
			try {
				serverHost = InetAddress.getByName(host);
				serverPort = port;
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}
	protected void close(DatagramSocket d) {
		if (d != null) {
			try {
				d.close();
			} catch (Exception e) {
			}
		}
	}
	private void openDatagramSocket() {
		try {
			datagram = new DatagramSocket();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public static synchronized DataUdpAgent getInstance() {
		if (inst == null) {
			inst = new DataUdpAgent();
		}
		return inst;
	}

	ScouterConfig config = ScouterUDPStorage.getConfig();

	public boolean write(byte[] p) {
		try {
			if (serverHost == null)
				return false;
			if (p.length > config.getUdpPacketMaxBytes()) {
				return writeMTU(p, config.getUdpPacketMaxBytes());
			}
			DataOutputX out = new DataOutputX();
			out.write(NetCafe.CAFE);
			out.write(p);
			byte[] buff = out.toByteArray();
			DatagramPacket packet = new DatagramPacket(buff, buff.length);
			packet.setAddress(serverHost);
			packet.setPort(serverPort);
			datagram.send(packet);
			return true;

		} catch (IOException e) {
			logger.log(Level.WARNING, "A120: UDP writing error. " + e.getMessage());
			return false;
		}
	}

	private boolean writeMTU(byte[] data, int packetSize) {
		try {
			if (serverHost == null)
				return false;
			long pkid = KeyGen.next();
			int total = data.length / packetSize;
			int remainder = data.length % packetSize;
			if (remainder > 0)
				total++;
			int num = 0;
			for (num = 0; num < data.length / packetSize; num++) {
				writeMTU(pkid, total, num, packetSize, DataInputX.get(data, num * packetSize, packetSize));
			}
			if (remainder > 0) {
				writeMTU(pkid, total, num, remainder, DataInputX.get(data, data.length - remainder, remainder));
			}
			return true;

		} catch (IOException e) {
			logger.log(Level.WARNING, "A121: UDP writing error.(MTU) " + e.getMessage());
			return false;
		}
	}

	private void writeMTU(long pkid, int total, int num, int packetSize, byte[] data) throws IOException {
		DataOutputX out = new DataOutputX();
		out.write(NetCafe.CAFE_MTU);
		out.writeInt(0);
		out.writeLong(pkid);
		out.writeShort(total);
		out.writeShort(num);
		out.writeBlob(data);
		byte[] buff = out.toByteArray();
		DatagramPacket packet = new DatagramPacket(buff, buff.length);
		packet.setAddress(serverHost);
		packet.setPort(serverPort);
		datagram.send(packet);
	}

	public void close() {
		if (datagram != null)
			datagram.close();
		datagram = null;
	}

	public boolean write(List<byte[]> p) {
		try {
			if (serverHost == null)
				return false;
			DataOutputX buffer = new DataOutputX();
			int bufferCount = 0;
			for (int i = 0; i < p.size(); i++) {
				byte[] b = p.get(i);
				if (b.length > config.getUdpPacketMaxBytes()) {
					writeMTU(b, config.getUdpPacketMaxBytes());

				} else if (b.length + buffer.getWriteSize() > config.getUdpPacketMaxBytes()) {
					sendList(bufferCount, buffer.toByteArray());
					buffer = new DataOutputX();
					bufferCount = 1;
					buffer.write(b);

				} else {
					bufferCount++;
					buffer.write(b);
				}
			}

			if (buffer.getWriteSize() > 0) {
				sendList(bufferCount, buffer.toByteArray());
			}
			return true;
		} catch (IOException e) {
			logger.log(Level.WARNING, "A123: UDP writing error." + e.getMessage());
			return false;
		}
	}

	private void sendList(int bufferCount, byte[] buffer) throws IOException {
		DataOutputX outter = new DataOutputX();
		outter.write(NetCafe.CAFE_N);
		outter.writeShort(bufferCount);
		outter.write(buffer);
		byte[] buff = outter.toByteArray();
		DatagramPacket packet = new DatagramPacket(buff, buff.length);
		packet.setAddress(serverHost);
		packet.setPort(serverPort);
		datagram.send(packet);
	}

	public boolean debugWrite(String ip, int port, int length) {
		try {
			DataOutputX out = new DataOutputX();
			out.write("TEST".getBytes());
			if (length > 4) {
				out.write(new byte[length - 4]);
			}
			byte[] buff = out.toByteArray();
			DatagramPacket packet = new DatagramPacket(buff, buff.length);
			packet.setAddress(InetAddress.getByName(ip));
			packet.setPort(port);
			datagram.send(packet);
			logger.info( "A124: Sent " + length + " bytes to " + ip + ":" + port);
			return true;

		} catch (IOException e) {
			logger.info( "A124: Sent " + length + " bytes to " + ip + ":" + port);

			logger.log(Level.WARNING, "A125: UDP writing error.(MTU) " + e.getMessage());
			return false;
		}
	}
}
