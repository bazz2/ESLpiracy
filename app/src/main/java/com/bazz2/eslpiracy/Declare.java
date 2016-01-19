package com.bazz2.eslpiracy;

import android.app.Application;

/**
 * Created by chenjt on 2015/11/26.
 */
public class Declare extends Application {
    private String[] links = new String[1024];
    private int i = 0;

    public int pushLink(String link) {
        int ret = 0;
        synchronized (this) {
            if (i >= 1024)
               ret = -1;
            else {
                this.links[i] = link;
                i++;
                ret = 0;
            }
        }
        return ret;
    }

    public void dumpLinks(String[] links) {
        synchronized(this) {
            initLinks();
            this.links = links;
        }
    }

    public void getLinks(String[] links) {
        synchronized(this) {
            links = this.links;
        }
    }

    public void initLinks() {
        synchronized(this) {
            for (int j = 0; j < links.length; j++) {
                this.links[j] = null;
            }
            i = 0;
        }
    }
}
