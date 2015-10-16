import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;


public class CO2MeterSerialTools {
    static SerialPort serialPort;
	public static void main(String[] args) {
        String[] portNames = SerialPortList.getPortNames();
        for(int i = 0; i < portNames.length; i++){
            System.out.println(portNames[i]);
        }
        
        serialPort = new SerialPort("COM7");
        try {
            serialPort.openPort();//Open serial port
            serialPort.setParams(9600, 8, 1, 0);//Set params.
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
            serialPort.setEventsMask(mask);//Set mask
            serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	static String tsChannelId;
	
	static void sendPPM(String ppm) {
		System.out.println(ppm);
//		HttpURLConnection url = (HttpURLConnection)new URL("http://api.thingspeak.com/update?api_key=8U1HL3MF593FILFK&field=" + ppm).openConnection();
//		url.
	}
	static void onSerialData(String data) throws SerialPortException {
		//System.out.println(data);
		if (data.indexOf("Press any key to display menu") > -1) {
   		  serialPort.writeBytes("\r".getBytes());
 		} else if (data.indexOf("(d) Enable display debugging info") > -1) {
		  serialPort.writeBytes("d\r".getBytes());
		} else if (data.indexOf("sPPM") > 0) {
			String ppm = data.substring(data.indexOf("sPPM        :") + "sPPM        :".length(), data.indexOf("\r", data.indexOf("sPPM        :")));
			sendPPM(ppm.trim());
			
		}
	}
	
	
    static class SerialPortReader implements SerialPortEventListener {

    	StringBuffer rcv = new StringBuffer();
    	Timer t = null;
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR()){//If data is available
                    try {
                        byte buffer[] = serialPort.readBytes(event.getEventValue());
                        rcv.append(new String(buffer));
                        if (t != null) t.cancel();
                        t = new Timer();
                        t.schedule(new TimerTask() { public void run() {
								try {
									onSerialData(rcv.toString());
								} catch (SerialPortException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
	                        	t= null;
	                            rcv = new StringBuffer();
							}
						}, 500);

                    }
                    catch (SerialPortException ex) {
                        System.out.println(ex);
                    }
            }
            else if(event.isCTS()){//If CTS line has changed state
                if(event.getEventValue() == 1){//If line is ON
                    System.out.println("CTS - ON");
                }
                else {
                    System.out.println("CTS - OFF");
                }
            }
            else if(event.isDSR()){///If DSR line has changed state
                if(event.getEventValue() == 1){//If line is ON
                    System.out.println("DSR - ON");
                }
                else {
                    System.out.println("DSR - OFF");
                }
            }
        }
    }

}
