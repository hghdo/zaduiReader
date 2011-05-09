package cn.zadui.reader.rss.helper;

import org.mcsoxford.rss.RSSItem;

public class ZaduiRssItem extends RSSItem {

	private String guid;
	
	  /* Internal constructor for RSSHandler */
	ZaduiRssItem(byte categoryCapacity, byte thumbnailCapacity) {
	    super(categoryCapacity,thumbnailCapacity);
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

}
