package special;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.net.ConnectException;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

/*
	RabbitMQ is an open-source message-broker software
	AMQP Advanced Message Queuing Protocol is the implementation
	requires server (and erlang) installed and running
	https://www.rabbitmq.com/getstarted.html
		> choco install rabbitmq
	Once Erlang & RabbitMQ are installed, a RabbitMQ node exists as a Windows service.
		ERLANG_HOME=C:\Program Files\erl-24.0\bin
		RabbitMQ_HOME=C:\Program Files\RabbitMQ Server\rabbitmq_server-3.10.5\sbin
		I added RabbitMQ_HOME to path
	choco installs ".erlang.cookie" into HOMEPATH; this cookie must be copied into target directory
		> cd C:\workspace\training
		> rabbitmqctl.bat help
		> rabbitmqctl.bat list_global_parameters
		> rabbitmqctl.bat status

	> rabbitmq-plugins enable rabbitmq_management
		> rabbitmqadmin get queue=queue_sample requeue=false
		> rabbitmqctl add_user full_access s3crEt
		> rabbitmqctl set_user_tags full_access administrator
		> rabbitmqctl set_permissions --vhost localhost full_access * * *
		> rabbitmqctl set_topic_permissions -p / full_access .* .* .*
		http://localhost:15672/				[full_access, s3crEt]
 */
public class RabbitMQHandler {

	private static final String EXCHANGE_QUEUE = "queue_sample";
	private static final String MQ_HOST = "localhost";
	private static final int MQ_PORT = 5672;
	private static final String MQ_USER = "username";
	private static final String MQ_PASS = "password";
	private static final boolean isExchanged = false;

	private static Channel channel = null;
	private static Connection connection = null;

	public static void main(String[] args) {
		//
		if ( connectionBuild(MQ_HOST, MQ_PORT) ) {
			messagesSend(10);
			//connectionClose();
		}
		if ( connectionBuild(MQ_HOST, MQ_PORT) ) {
			messagesRead();
			//connectionClose();
		}
		System.out.println("DONE");
	}

	private static boolean connectionBuild(String host, int port) {
		//
		System.out.println("Building connection & channel");
		boolean isValidConnection = false;
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost(host);
		//connectionFactory.setPort(port);
		// connectionFactory.setUsername(MQ_USER);
		// connectionFactory.setPassword(MQ_PASS);
		try {
			connection = connectionFactory.newConnection();
			channel = connection.createChannel();
			if ( isExchanged ) {
				String EXCHANGE_TYPE = "fanout";
				channel.exchangeDeclare(EXCHANGE_QUEUE, EXCHANGE_TYPE);
				String queueName = channel.queueDeclare().getQueue();
				channel.queueBind(queueName, EXCHANGE_QUEUE, "");
				System.out.println("queueName: " + queueName);
			}
			isValidConnection = true;
			System.out.println("Connection & Channel created!");
		}
		catch (IOException | TimeoutException ex) {
			//
			if ( ex instanceof ConnectException ) {
				System.out.println("Please start broker: " + ex.getMessage());
			} else {
				System.out.println("ERROR build: " + ex.getMessage());
			}
		}
		return isValidConnection;
	}

	private static void connectionClose( ) {
		//
		System.out.println("Closing channel & connection");
		try {
			channel.close();
			connection.close();
		}
		catch (IOException | TimeoutException ex) { System.out.println("ERROR close: " + ex.getMessage()); }
	}

	private static void messagesSend(int qty) {
		//
		String FRMT = "message[%02d]: %s";
		String ROUTING_KEY = "";
		int ictr = 0;
		AMQP.BasicProperties AMQP_BP = null;
		try {
			channel.queueDeclare(EXCHANGE_QUEUE, false, false, false, null);
			while ( ictr < qty ) {
				//
				String txtMessage = String.format(FRMT, ( ++ictr ), Instant.now());
				channel.basicPublish(ROUTING_KEY, EXCHANGE_QUEUE, AMQP_BP, txtMessage.getBytes());
			}
		}
		catch (IOException ex) { System.out.println("ERROR send: " + ex.getMessage()); }
		System.out.println("[x] Sent [" + ictr + "] messages!");
	}

	private static void messagesRead( ) {
		//
		try {
			channel.queueDeclare(EXCHANGE_QUEUE, false, false, false, null);
			System.out.println("[*] Waiting for messages. To exit press CTRL+C");
			//
			DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				String txtMessage = new String(delivery.getBody(), UTF_8);
				System.out.println("[x] Received '" + txtMessage + "'");
			};
			//
			channel.basicConsume(EXCHANGE_QUEUE, true, deliverCallback, consumerTag -> { });
		}
		catch (IOException ex) { System.out.println("ERROR read: " + ex.getMessage()); }
	}
}
