public class Homography {

    private static final double CAM_PIXEL_HEIGHT = 3024;
    private static final double IMAGE_BOTTOM = 6.5;
    private static final double IMAGE_TOP = 33;
    private static final double CAM_OFFSET_Y = 6;
    private static final double CAM_OFFSET_Z = 22.5;
    private static final double FY = 5.5175;
    private static final double MF = -CAM_OFFSET_Z / (17 - CAM_OFFSET_Y);
    private static final double MF_NEG_RECIPROCAL = -(1 / MF);
    private static final double FZ = MF * (FY - CAM_OFFSET_Y) + CAM_OFFSET_Z;
    private static final double PYS = (MF_NEG_RECIPROCAL * CAM_OFFSET_Y + FZ + (FY * FZ) / (IMAGE_BOTTOM - FY) - CAM_OFFSET_Z) / ((FZ / (IMAGE_BOTTOM - FY)) + MF_NEG_RECIPROCAL);
    private static final double PYE = (FZ + (FY * FZ) / (IMAGE_TOP - FY) - CAM_OFFSET_Z + MF_NEG_RECIPROCAL * CAM_OFFSET_Y) / ((FZ / (IMAGE_TOP - FY)) + MF_NEG_RECIPROCAL);
    private static final double PZ_PYS = MF_NEG_RECIPROCAL * (PYS - CAM_OFFSET_Y) + CAM_OFFSET_Z;
    private static final double PZ_PYE = MF_NEG_RECIPROCAL * (PYE - CAM_OFFSET_Y) + CAM_OFFSET_Z;
    private static final double PL = Math.sqrt(Math.pow(PYE - PYS, 2) + Math.pow(PZ_PYE - PZ_PYS, 2));

    public static DoublePoint convertCameraPointToWorldPoint(double x, double y) {
        double p = 1 - (y / 3024);
        double ppz = PZ_PYS + (PZ_PYE - PZ_PYS) * p;
        double ppy = PYS + (PYE - PYS) * p;
        double wy = -FZ * ((FY - ppy) / (FZ - ppz)) + FY;
        return new DoublePoint(0, wy);
    }

}
