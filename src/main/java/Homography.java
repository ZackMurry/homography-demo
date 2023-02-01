import Jama.Matrix;
import org.opencv.core.Point;

public class Homography {
    //Value from solvePnP
    private static final Matrix TRANSLATION_VECTOR = new Matrix( new double[][] {
            {-0.4172315},
            {3.02005787},
            {15.04841774}
    } );
    //Value after applying Rodriguez rotation formula to rotation vector from solvePnP
    private static final Matrix ROTATION_MATRIX = new Matrix( new double[][] {
            {-0.01833427, 0.99945017, -0.02762646},
            {-0.41551407, 0.01751549, 0.90941809},
            {0.90940195, 0.0281527, 0.41496448}
    } );
    //Value from camera calibration
    private static final Matrix CAMERA_MATRIX = new Matrix( new double[][] {
            {1.45508789e3, 0, 6.98702405e2},
            {0, 1.44057202e3, 3.38917510e2},
            {0, 0, 1}
    } );

    //Z axis is always 1 since all the objects are on the ground
    final static double Z_CONST = 1;


    /**
     * Calculates the position of the corresponding point in 3D space from a camera point
     * @param point camera point at which to find the corresponding 3D point
     * @return point's position relative to the camera
     */
    public static DoublePoint positionFromPoint( Point point ) {

        //Change point into a matrix
        Matrix pointMatrix = new Matrix( new double[][] {
                {point.x},
                {point.y},
                {1}
        } );

        /*
        left = np.linalg.inv(rotation_matrix).dot(np.linalg.inv(intrinsic_matrix)).dot(image_point)
	# print("left:\n" + str(left))
	right = np.linalg.inv(rotation_matrix).dot(translation_matrix)
	# print("right:\n" + str(right))
	scalar = (1 + right[2,0]) / left[2,0]
	# print("scalar:\n" + str(scalar))

	world_point = np.linalg.inv(rotation_matrix).dot(np.linalg.inv(intrinsic_matrix).dot(image_point).dot(scalar)-translation_matrix)
	world_point -= np.array([[284.32701103,-10.62673169,0]]).T
	# world_point /= 21.61484 # Square side length in mm
	world_point /= 21.4639466
         */
        Matrix left = ROTATION_MATRIX.inverse().times(CAMERA_MATRIX.inverse()).times(pointMatrix);
        Matrix right = ROTATION_MATRIX.inverse().times(TRANSLATION_VECTOR);
        double scalar = (Z_CONST + right.get(2, 0)) / left.get(2, 0);

        Matrix pos = ROTATION_MATRIX.inverse().times(CAMERA_MATRIX.inverse().times(pointMatrix).times(scalar).minus(TRANSLATION_VECTOR));
//        pos = pos.times(1/21.4639466);

        return new DoublePoint(pos.get(1, 0), pos.get(0, 0));

        //Calculating scalar value
//        Matrix leftSideMatrix = ROTATION_MATRIX.inverse().times( CAMERA_MATRIX.inverse() ).times(pointMatrix);
//        Matrix rightSideMatrix = ROTATION_MATRIX.inverse().times( TRANSLATION_VECTOR );
//        double scalar = (Z_CONST + rightSideMatrix.get( 2, 0 )) / leftSideMatrix.get( 2, 0 );
//
//        //(x,y) position in mm
//        Matrix calculatedPosition = ROTATION_MATRIX.inverse().times( CAMERA_MATRIX.inverse().times( pointMatrix ).times( scalar ).minus( TRANSLATION_VECTOR ) );
//
//        //Divide by 25.4 to get inches from mm
//        calculatedPosition = calculatedPosition.times( 1 / 25.4 );
//
//        //Swap X and Y, and add 24 to X to account for camera coordinate system
//        double x = calculatedPosition.get( 0, 0 );
//        double y = calculatedPosition.get( 1, 0 );
//        return new DoublePoint( y + 12.9, x - 4.1);
    }
}
