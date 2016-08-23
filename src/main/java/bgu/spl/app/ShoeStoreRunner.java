package bgu.spl.app;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import bgu.spl.app.JSON.AppData;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.impl.MessageBusImpl;
/**
 * This class is our main class of the proggram. It will run the store for a single period which is 
 * specipied in the JSON file attached using gson and count down latch. 
 */
public class ShoeStoreRunner {
	
	public final static Logger LOGGER = Logger.getLogger(ShoeStoreRunner.class.getName());
	/**
	 * Runs the program: loads the Json file and runs the threads.
	 * @throws e File not found exception.
	 * @param args console input. 
	 */
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		Store store = Store.getInstance();
		Gson gson = new Gson();  
		BufferedReader br = null;
		System.setProperty("java.util.logging.SimpleFormatter.format","%4$s: %5$s %n");
		try {
			br = new BufferedReader(new FileReader(/*"/users/studs/bsc/2016/yairweis/"*/"c://json/"+sc.nextLine()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		AppData appData = gson.fromJson(br, AppData.class);
		int num = 1 + appData.getServices().getFactories() + appData.getServices().getSellers() + appData.getServices().getCustomers().length;
		CountDownLatch cdl = new CountDownLatch(num);
		ArrayList<Thread> threads = new ArrayList<Thread>();

		//initial store
		ShoeStorageInfo[] storage = new ShoeStorageInfo[appData.getInitialStorage().length];		
		for(int i = 0; i < appData.getInitialStorage().length; i++){
			storage[i] = new ShoeStorageInfo(appData.getInitialStorage()[i].getShoeType(), appData.getInitialStorage()[i].getAmount(),0);
		}
		store.load(storage);

		//initial timer
		TimeService timeService = new TimeService(new AtomicInteger(appData.getServices().getTime().getSpeed()),
				new AtomicInteger(appData.getServices().getTime().getDuration()),cdl);
		Thread timeThread = new Thread(timeService);

		//initial manager
		ArrayList<DiscountSchedule> discounts = new ArrayList<DiscountSchedule>();
		for(int i = 0; i < appData.getServices().getManager().getJsonDiscountSchedule().length; i++){
			DiscountSchedule discount = new DiscountSchedule(
					appData.getServices().getManager().getJsonDiscountSchedule()[i].getShoeType(),
					appData.getServices().getManager().getJsonDiscountSchedule()[i].getTick(),
					appData.getServices().getManager().getJsonDiscountSchedule()[i].getAmount());
			discounts.add(discount);
		}
		
		ManagementService manager = new ManagementService(discounts,cdl);
		Thread managerThread = new Thread(manager);
		threads.add(managerThread);

		//initial factories
		for(int i = 0; i < appData.getServices().getFactories(); i++){
			Thread factory = new Thread(new ShoeFactoryService("Factory"+(i+1),cdl)); 
			threads.add(factory);
		}
		
		//initial sellers
		for(int i = 0; i < appData.getServices().getSellers(); i++){
			Thread seller = new Thread(new SellingService("Seller"+(i+1),cdl)); 
			threads.add(seller);
		}

		//initial costumers
		for(int i = 0; i < appData.getServices().getCustomers().length; i++){
			ArrayList<String> wishList = new ArrayList<String>();
			for(int j = 0; j < appData.getServices().getCustomers()[i].getWishList().length; j++){
				String shoe = new String(appData.getServices().getCustomers()[i].getWishList()[j]);
				wishList.add(shoe);
			}
			ArrayList<PurchaseSchedule> orders = new ArrayList<PurchaseSchedule>();
			for(int k = 0; k < appData.getServices().getCustomers()[i].getPurchaseSchedule().length; k++){
				PurchaseSchedule order = new PurchaseSchedule(
						appData.getServices().getCustomers()[i].getPurchaseSchedule()[k].getShoeType(),
						appData.getServices().getCustomers()[i].getPurchaseSchedule()[k].getTick());
				orders.add(order);
			}
			WebClientService costumer = new WebClientService(appData.getServices().getCustomers()[i].getName(),orders,wishList,cdl);
			Thread client = new Thread(costumer);
			threads.add(client);
		}

		//start the timer thread and then the other threads
		timeThread.start();
		for(int i = 0; i < threads.size(); i++){
			threads.get(i).start();
		}
	}
}
