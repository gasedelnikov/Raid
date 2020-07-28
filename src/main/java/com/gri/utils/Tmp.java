package com.gri.utils;

import com.gri.Main;
import com.gri.model.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tmp {
    private static Logger logger = LoggerFactory.getLogger(Tmp.class);

//
//    public static void calc0(Attribute[][] attribute, double[] targetDelta) {
//        Attribute[][] a = attribute;
//        for (int i3 = 0; i3 < attribute[3].length; i3++) {
//            Attribute v3 = attribute[3][i3];
//            for (int i4 = 0; i4 < attribute[4].length; i4++) {
//                Attribute v4 = attribute[4][i4];
//                logger.info(" {} {}", i3, i4);
//                for (int i5 = 0; i5 < attribute[5].length; i5++) {
//                    Attribute v5 = attribute[5][i5];
////logger.info(" {} {} {}", i3, i4, i5);
////                    if (v3.id == 173) {
////                        if (v4.id == 174) {
////                            if (v5.id == 175) {
////                                int x = 1;
////                            }
////                        }
////                    }
//// a = attribute;
//                    a = create2dArray(Utils.getDelta(targetDelta, Main.getAttributeBonuses(v3, v4, v5))
//                            , attribute[0], attribute[1], attribute[2]
//                            , new Attribute[]{v3}, new Attribute[]{v4}, new Attribute[]{v5}, attribute[6], attribute[7], attribute[8]);
//
//                    for (int i0 = 0; i0 < a[0].length; i0++) {
//                        Attribute v0 = a[0][i0];
//                        for (int i1 = 0; i1 < a[1].length; i1++) {
//                            Attribute v1 = a[1][i1];
//                            for (int i2 = 0; i2 < a[2].length; i2++) {
//                                Attribute v2 = a[2][i2];
//                                for (int i6 = 0; i6 < a[6].length; i6++) {
//                                    Attribute v6 = a[6][i6];
//                                    for (int i7 = 0; i7 < a[7].length; i7++) {
//                                        Attribute v7 = a[7][i7];
//                                        for (int i8 = 0; i8 < a[8].length; i8++) {
//
//                                            Main.calcAttributeCortege(targetDelta, v0, v1, v2, v3, v4, v5, v6, v7, a[8][i8]);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        System.out.println("");
//    }
//
//    public static void calc1(Attribute[][] attribute, double[] targetDelta) {
//        long t0 = System.currentTimeMillis();
//
//        Attribute[][] a = attribute;
//        Attribute[][] b = attribute;
//        for (int i3 = 0; i3 < attribute[3].length; i3++) {
//            Attribute v3 = attribute[3][i3];
//            logger.info(" i = {}; time = {}", i3, (System.currentTimeMillis() - t0) / 1000);
//            for (int i4 = 0; i4 < attribute[4].length; i4++) {
//                Attribute v4 = attribute[4][i4];
////                logger.info(" {} {}", i3, i4);
//                for (int i5 = 0; i5 < attribute[5].length; i5++) {
//                    Attribute v5 = attribute[5][i5];
//                    a = create2dArray(Utils.getDelta(targetDelta, Main.getAttributeBonuses(v3, v4, v5))
//                            , attribute[0], attribute[1], attribute[2]
//                            , new Attribute[]{v3}, new Attribute[]{v4}, new Attribute[]{v5}
//                            , attribute[6], attribute[7], attribute[8]);
//
//                    for (int i0 = 0; i0 < a[0].length; i0++) {
//                        Attribute v0 = a[0][i0];
//                        for (int i1 = 0; i1 < a[1].length; i1++) {
//                            Attribute v1 = a[1][i1];
//                            for (int i2 = 0; i2 < a[2].length; i2++) {
//                                Attribute v2 = a[2][i2];
//
//                                b = create2dArray(Utils.getDelta(targetDelta, Main.getAttributeBonuses(v0, v1, v2, v3, v4, v5))
//                                        , new Attribute[]{v0}, new Attribute[]{v1}, new Attribute[]{v2}
//                                        , new Attribute[]{v3}, new Attribute[]{v4}, new Attribute[]{v5}
//                                        , attribute[6], attribute[7], attribute[8]);
//
//                                for (int i6 = 0; i6 < b[6].length; i6++) {
//                                    Attribute v6 = b[6][i6];
//                                    for (int i7 = 0; i7 < b[7].length; i7++) {
//                                        Attribute v7 = b[7][i7];
//                                        for (int i8 = 0; i8 < b[8].length; i8++) {
//                                            Main.calcAttributeCortege(targetDelta, v0, v1, v2, v3, v4, v5, v6, v7, b[8][i8]);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        logger.info("END; time = {}", (System.currentTimeMillis() - t0) / 1000);
//    }
//
//    public static void calc2(Attribute[][] attributes, double[] targetDelta) {
//
//        for (int i3 = 0; i3 < attributes[3].length; i3++) {
//            for (int i4 = 0; i4 < attributes[4].length; i4++) {
////logger.info(" {} {}", i3, i4);
//                for (int i5 = 0; i5 < attributes[5].length; i5++) {
//                    logger.info(" {} {} {}", i3, i4, i5);
//                    for (int i0 = 0; i0 < attributes[0].length; i0++) {
//                        for (int i1 = 0; i1 < attributes[1].length; i1++) {
//                            for (int i2 = 0; i2 < attributes[2].length; i2++) {
//                                for (int i6 = 0; i6 < attributes[6].length; i6++) {
//                                    for (int i7 = 0; i7 < attributes[7].length; i7++) {
//                                        for (int i8 = 0; i8 < attributes[8].length; i8++) {
//
//                                            double[] x0 = new double[Constants.VAL_COUNT];
//                                            for (int j = 0; j < Constants.VAL_COUNT; j++) {
//                                                x0[j] += attributes[0][i0].values[j] +
//                                                        attributes[1][i1].values[j] +
//                                                        attributes[2][i2].values[j] +
//                                                        attributes[3][i3].values[j] +
//                                                        attributes[4][i4].values[j] +
//                                                        attributes[5][i5].values[j] +
//                                                        attributes[6][i6].values[j] +
//                                                        attributes[7][i7].values[j] +
//                                                        attributes[8][i8].values[j]
//                                                ;
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        System.out.println("");
//    }
//
//    public static Attribute[][] create2dArray(double[] targetDelta, Attribute[] a0, Attribute[] a1, Attribute[] a2, Attribute[] a3, Attribute[] a4, Attribute[] a5, Attribute[] a6, Attribute[] a7, Attribute[] a8) {
//        Attribute[][] result = new Attribute[Constants.PLACES_COUNT][];
//        result[0] = a0;
//        result[1] = a1;
//        result[2] = a2;
//        result[3] = a3;
//        result[4] = a4;
//        result[5] = a5;
//        result[6] = a6;
//        result[7] = a7;
//        result[8] = a8;
//
//        return Main.filterAttributesRecursive(result, targetDelta);
//    }

}
