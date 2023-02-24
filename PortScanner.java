import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PortScanner {
	public static void main(String[] args) throws IOException {
    		if(args[0].equals("-i")) {
			runPortScan(args[1], 65535);
		}
		
	}

	public static void runPortScan(String ip, int nbrPortMaxToScan) throws IOException {
		ConcurrentLinkedQueue<Integer> openPorts = new ConcurrentLinkedQueue<>();
		ExecutorService executorService = Executors.newFixedThreadPool(16);
		AtomicInteger port = new AtomicInteger(0);
		while (port.get() < nbrPortMaxToScan) {
			final int currentPort = port.getAndIncrement();
			executorService.submit(() -> {
				try {
          				Socket socket = new Socket();
					socket.connect(new InetSocketAddress(ip, currentPort), 10);
					socket.close();
					openPorts.add(currentPort);
					System.out.println(ip + " ,port open: " + currentPort);
				} catch (ConnectException e) {

				} catch (IOException e) {
					System.err.println(e);
				}
			});
		}
		executorService.shutdown();
		try {
			executorService.awaitTermination(10, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		List<Integer> openPortList = new ArrayList<>();
		System.out.println("openPortsQueue: " + openPorts.size());
		while (!openPorts.isEmpty()) {
			openPortList.add(openPorts.poll());
		}
		openPorts.forEach(p -> System.out.println("port " + p + " is open"));
	}
}
