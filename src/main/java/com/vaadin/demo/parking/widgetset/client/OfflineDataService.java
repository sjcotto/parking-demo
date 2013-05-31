package com.vaadin.demo.parking.widgetset.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;
import com.vaadin.client.VConsole;
import com.vaadin.demo.parking.widgetset.client.model.Ticket;

public class OfflineDataService {

    private static final String LOCALSTORAGE_PREFIX = "VST_";
    private static final String TICKETCOUNT_KEY = LOCALSTORAGE_PREFIX
            + "obscount";

    public interface Callback {
        public void setSpecies(List<Species> birds);
    }

    public static void getSpecies(final Callback cb) {
        /*
         * This should work offline as bird list is mentioned in cache manifest
         */
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET,
                GWT.getModuleBaseURL() + "birds_en.html");
        rb.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                String text = response.getText();
                String[] lines = text.split("\n");
                ArrayList<Species> arrayList = new ArrayList<Species>();
                for (String string : lines) {
                    String[] split = string.split("\t");
                    if (split.length != 2 || split[0].isEmpty()
                            || split[1].isEmpty()) {
                        continue;
                    }
                    Species species = new Species();
                    species.setId(split[0]);
                    species.setName(split[1]);
                    arrayList.add(species);
                }
                cb.setSpecies(arrayList);
            }

            @Override
            public void onError(Request request, Throwable exception) {
                VConsole.log("Offline fetching species failed");
            }
        });
        try {
            rb.send();
        } catch (RequestException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void localStoreTicket(Ticket ticket) {
        StorageMap s = new StorageMap(Storage.getLocalStorageIfSupported());
        String ticketCount = s.get(TICKETCOUNT_KEY);
        int id;
        if (ticketCount == null) {
            id = 0;
            ticketCount = "" + 1;
            s.put(TICKETCOUNT_KEY, ticketCount);
        } else {
            id = Integer.parseInt(ticketCount);
        }
        s.put(LOCALSTORAGE_PREFIX + id, ticket.serialize());
        id++;
        s.put(TICKETCOUNT_KEY, "" + id);
    }

    public static int getStoredTicketCount() {
        int result = 0;
        StorageMap s = new StorageMap(Storage.getLocalStorageIfSupported());
        String ticketCount = s.get(TICKETCOUNT_KEY);
        if (ticketCount != null) {
            result = Integer.parseInt(ticketCount);
        }
        return result;
    }

    public static List<Ticket> getAndResetLocallyStoredTickets() {
        ArrayList<Ticket> al = new ArrayList<Ticket>();
        StorageMap s = new StorageMap(Storage.getLocalStorageIfSupported());
        String obscount = s.get(TICKETCOUNT_KEY);
        if (obscount != null) {
            int c = Integer.parseInt(obscount);
            for (int i = 0; i < c; i++) {
                String key = LOCALSTORAGE_PREFIX + i;
                String json = s.get(key);
                Ticket fromJSON = Ticket.deserialize(json);
                al.add(fromJSON);
                s.remove(key);
            }
            s.remove(obscount);
        }
        s.put(TICKETCOUNT_KEY, "" + 0);
        return al;
    }

}
