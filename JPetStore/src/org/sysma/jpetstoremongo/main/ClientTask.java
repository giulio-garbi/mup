package org.sysma.jpetstoremongo.main;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.MainTaskDefinition;

public class ClientTask extends MainTaskDefinition<String[]> {
	
	/*private static String[][] homePageFiles = {
		{"css/jpetstore.css", "images/logo-topbar.gif", "images/cart.gif"},
		{"images/separator.gif", "images/sm_fish.gif", "images/sm_dogs.gif"}, 
			{"images/sm_reptiles.gif", "images/sm_cats.gif", "images/sm_birds.gif"},
			{"images/fish_icon.gif", "images/dogs_icon.gif", "images/reptiles_icon.gif"}, 
				{"images/cats_icon.gif", "images/birds_icon.gif", "images/splash.gif"}
	};*/
	
	static <E> E getRandomSetElement(Set<E> set) {
	    return set.stream().skip(ThreadLocalRandom.current().nextInt(set.size())).findFirst().orElse(null);
	}
	
	private void searchAndBuy(Communication comm, boolean buy) throws IOException, InterruptedException, ExecutionException {
		var src = Map.of(
			"a", Map.of(
				"FI-SW-01", Set.of("EST-1", "EST-2"),
				"FI-SW-02", Set.of("EST-3"),
				"K9-DL-01", Set.of("EST-9", "EST-10"),
				"K9-RT-02", Set.of("EST-22", "EST-23", "EST-24", "EST-25"),
				"K9-CW-01", Set.of("EST-26", "EST-27"),
				"RP-SN-01", Set.of("EST-11", "EST-12"),
				"RP-LI-02", Set.of("EST-13"),
				"FL-DSH-01", Set.of("EST-14", "EST-15"),
				"FL-DLH-02", Set.of("EST-16", "EST-17"),
				"AV-CB-01", Set.of("EST-18")
			),
			"e", Map.of(
				"FI-SW-01", Set.of("EST-1", "EST-2"),
				"FI-SW-02", Set.of("EST-3"),
				"K9-PO-02", Set.of("EST-8"),
				"K9-RT-01", Set.of("EST-28"),
				"K9-RT-02", Set.of("EST-22", "EST-23", "EST-24", "EST-25"),
				"RP-SN-01", Set.of("EST-11", "EST-12"),
				"FL-DLH-02", Set.of("EST-16", "EST-17")
			),
			"i", Map.of(
				"FI-SW-01", Set.of("EST-1", "EST-2"),
				"FI-SW-02", Set.of("EST-3"),
				"FI-FW-01", Set.of("EST-4", "EST-5"),
				"FI-FW-02", Set.of("EST-20", "EST-21"),
				"K9-DL-01", Set.of("EST-9", "EST-10"),
				"K9-RT-01", Set.of("EST-28"),
				"K9-RT-02", Set.of("EST-22", "EST-23", "EST-24", "EST-25"),
				"K9-CW-01", Set.of("EST-26", "EST-27"),
				"RP-LI-02", Set.of("EST-13"),
				"FL-DLH-02", Set.of("EST-16", "EST-17")
			),
			"o", Map.of(
				"FI-FW-01", Set.of("EST-4", "EST-5"),
				"FI-FW-02", Set.of("EST-20", "EST-21"),
				"K9-BD-01", Set.of("EST-6", "EST-7"),
				"K9-PO-02", Set.of("EST-8"),
				"K9-DL-01", Set.of("EST-9", "EST-10"),
				"K9-RT-01", Set.of("EST-28"),
				"K9-RT-02", Set.of("EST-22", "EST-23", "EST-24", "EST-25"),
				"AV-CB-01", Set.of("EST-18")
			),
			"u", Map.of(
				"K9-BD-01", Set.of("EST-6", "EST-7"),
				"K9-CW-01", Set.of("EST-26", "EST-27"),
				"RP-LI-02", Set.of("EST-13")
			)
		);
		var srcEnt = getRandomSetElement(src.entrySet());
		var prodEnt = getRandomSetElement(srcEnt.getValue().entrySet());
		var item = getRandomSetElement(prodEnt.getValue());
		
		comm.asyncCallRegistry("frontend", "SearchProds", (x)->{}, 
				"keyword", srcEnt.getKey()).get().close();
		Thread.sleep(getTime(50));
		comm.asyncCallRegistry("frontend", "ViewProduct", (x)->{}, 
				"productId", prodEnt.getKey()).get().close();
		Thread.sleep(getTime(50));
		if(buy) {
			comm.asyncCallRegistry("frontend", "AddToCart", (x)->{}, 
					"workingItemId", item).get().close();
			Thread.sleep(getTime(50));
		}
	}
	
	private void browse(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var structure = Map.of(
			"FISH", Map.of(
				"FI-SW-01", Set.of("EST-1", "EST-2"),
				"FI-SW-02", Set.of("EST-3"),
				"FI-FW-01", Set.of("EST-4", "EST-5"),
				"FI-FW-02", Set.of("EST-20", "EST-21")
			),
			"DOGS", Map.of(
				"K9-BD-01", Set.of("EST-6", "EST-7"),
				"K9-PO-02", Set.of("EST-8"),
				"K9-DL-01", Set.of("EST-9", "EST-10"),
				"K9-RT-01", Set.of("EST-28"),
				"K9-RT-02", Set.of("EST-22", "EST-23", "EST-24", "EST-25"),
				"K9-CW-01", Set.of("EST-26", "EST-27")
			),
			"CATS", Map.of(
				"FL-DSH-01", Set.of("EST-14", "EST-15"),
				"FL-DLH-02", Set.of("EST-16", "EST-17")
			),
			"REPTILES", Map.of(
				"RP-SN-01", Set.of("EST-11", "EST-12"),
				"RP-LI-02", Set.of("EST-13")
			),
			"BIRDS", Map.of(
				"AV-CB-01", Set.of("EST-18"),
				"AV-SB-02", Set.of("EST-19")
			)
		);
		
		var catEnt = getRandomSetElement(structure.entrySet());
		var prodEnt = getRandomSetElement(catEnt.getValue().entrySet());
		var item = getRandomSetElement(prodEnt.getValue());
		
		comm.asyncCallRegistry("frontend", "ViewCategory", (x)->{}, 
				"categoryId", catEnt.getKey()).get().close();
		Thread.sleep(getTime(50));
		comm.asyncCallRegistry("frontend", "ViewProduct", (x)->{}, 
				"productId", prodEnt.getKey()).get().close();
		Thread.sleep(getTime(50));
		comm.asyncCallRegistry("frontend", "ViewItem", (x)->{}, 
				"itemId", item).get().close();
		Thread.sleep(getTime(50));
		comm.asyncCallRegistry("frontend", "ViewProduct", (x)->{}, 
				"productId", prodEnt.getKey()).get().close();
		Thread.sleep(getTime(50));
		comm.asyncCallRegistry("frontend", "ViewCategory", (x)->{}, 
				"categoryId", catEnt.getKey()).get().close();
		Thread.sleep(getTime(50));
	}
	
	/*
	 * http://localhost:9080/rest/api/flights/queryflights
	 */
	
	private static int getTime(int msec) {
		return msec*70;
		//var u = 1.-ThreadLocalRandom.current().nextDouble();
		//return (int) (-Math.log(u)*msec);
	}

	@Override
	public void main(Communication comm, String[] arg) throws InterruptedException {
		//String username = arg[0];
		//String password = arg[1];
		Thread.sleep(getTime(10));
		try {
			comm.asyncCallRegistry("frontend", "Main", (x)->{}).get().close();
			Thread.sleep(getTime(50));
			comm.asyncCallRegistry("frontend", "SignonForm", (x)->{}).get().close();
			//Thread.sleep(getTime(50));
			//browse(comm);
			//comm.asyncCallRegistry("frontend", "Main", (x)->{}).get().close();
			//Thread.sleep(getTime(50));
			comm.asyncCallRegistry("frontend", "Login", (x)->{},
					"username","j2ee","password","j2ee").get().close();
			//comm.asyncCallRegistry("frontend", "Main", (x)->{}).get().close();
			Thread.sleep(getTime(50));
			/*browse(comm);
			comm.asyncCallRegistry("frontend", "Main", (x)->{}).get().close();
			Thread.sleep(getTime(50));
			//browse(comm);
			comm.asyncCallRegistry("frontend", "Main", (x)->{}).get().close();
			Thread.sleep(getTime(50));*/
			int whenBuy = ThreadLocalRandom.current().nextInt(2);
			searchAndBuy(comm, whenBuy == 0);
			//searchAndBuy(comm, whenBuy == 1);
			Thread.sleep(getTime(50));
			comm.asyncCallRegistry("frontend", "Signout", (x)->{}).get().close();
			//comm.asyncCallRegistry("frontend", "Main", (x)->{}).get().close();
			
		} catch (IOException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
	}
	/*
	private static void callPagesGrouped(Communication comm, String[]... pageGroups) {
		Arrays.stream(pageGroups).forEachOrdered((pg)->{
			ArrayList<CompletableFuture<CloseableHttpResponse>> futures = new ArrayList<>();
			Arrays.stream(pg).forEachOrdered(p->{
				futures.add(comm.asyncCallPage("frontend", p, x->{}));
			});
			for(int i=0; i<futures.size(); i++) {
				var cf = futures.get(i);
				try {
					//System.out.println(pg[i] + " ?");
					var rsp = cf.get();
					//System.out.println(pg[i] + " --- " + rsp.getCode());
					rsp.close();
				} catch (IOException | InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}*/

}
