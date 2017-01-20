package cd.blog.humbird.vertx.proxy.beans;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by david on 17/1/3.
 */
public class BusTest {

    Vertx vertx;

    @Before
    public void init() {
        vertx = Vertx.vertx();
    }

    public void busSendTest() {
        EventBus eventBus = vertx.eventBus();
//        eventBus.registerDefaultCodec(Serializable.class, new SerializableMessageCode());
//        VxQueryParams vxQueryParams = new VxQueryParams();
//        vxQueryParams.setLimit(1000);
//        MessageProducer<Object> producer = eventBus.sender("xxmmm.19001", new DeliveryOptions().setCodecName("serializable"));
//        producer.send(vxQueryParams, handler -> {
//            if (handler.succeeded()) {
//                System.out.println("reply: " + handler.result().body());
//            } else {
//
//            }
//        });
    }

    public void busConsumerTest() throws InterruptedException {
        EventBus eventBus = vertx.eventBus();
        TestState testState = new TestState(eventBus);
        testState.handler();
    }

    class TestState {
        AbConsumer<Object> abConsumer;

        public TestState(EventBus eventBus) {
            this.abConsumer = new AbConsumer(eventBus, "xxmmm.19001");;
        }

        public void handler() {
//            this.abConsumer.consume(message->{
//                System.out.println("from: " + message.address() + ", message: " + ((VxQueryParams) message.body()).getLimit());
//                message.reply("Go U.");
//            }).completionHandler(handler -> {
//                if (handler.succeeded()) {
//                    System.out.println("succeeded");
//                } else {
//                    System.out.println("failed");
//                }
//            });
        }
    }

    class AbConsumer<T> {
        MessageConsumer<T> consumer;

        public AbConsumer(EventBus bus, String address) {
            this.consumer = bus.consumer(address);
        }

        public MessageConsumer<T> consume(Handler<Message<T>> handler) {
            return consumer.handler(handler);
        }
    }

    @Test
    public void abcTest() throws InterruptedException {
        busConsumerTest();
        busSendTest();
    }

    @Test
    public void periodicTest() throws InterruptedException {
        final int[] i = {0};
        long timeId = vertx.setPeriodic(1000, handler -> {
            System.out.println(i[0]++);
        });
        System.out.println("First this is printed");

        Thread.sleep(5000);
        vertx.cancelTimer(timeId);
        Thread.sleep(10000);
        System.out.println("end-----");
    }

    @Test
    public void timeTest() throws InterruptedException {
        long timeId = -1;
        timeId = vertx.setTimer(3000, handler -> {
            System.out.println("----");
        });
        Thread.sleep(10000);
        System.out.println(" +++ " + timeId);

    }

    @Test
    public void dTest() {

    }
}
