package com.gri.model;

public class RankMaskFilter {

    public int placeId;
    public int[] values;

    public RankMaskFilter(int placeId, int[] values) {
        this.placeId = placeId;
        this.values = values;
    }

    public RankMaskFilter(int placeId, int c, int v, int h, int a, int d, int r, int s, int m) {
        this.placeId = placeId;
        this.values = new int[]{c, v, h, a, d, r, s, m};
    }
}
