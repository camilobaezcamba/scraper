package py.gov.ocds.context;

import py.gov.ocds.scraper.Scraper;

public class Context {

    public static void main(String[] args) throws InterruptedException {
        Scraper scraper = new Scraper();
        scraper.scrap();

    }
}