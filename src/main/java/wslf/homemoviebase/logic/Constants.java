package wslf.homemoviebase.logic;

/**
 *
 * @author WslF
 */
public class Constants {

    /**
     * types of common video files
     */
    public static final String[] DATA_TYPES = {"avi", "mpg", "mpeg", "mp4", "3gp"};
    /**
     * Maximum files size to load whole it to the RAM. in bytes
     */
    public static final long MAX_SIZE_IN_RAM = 256 * 1024 * 1024; // 256 MB
    /**
     * message for success status
     */
    public static final String SUCCESS_MESSAGE = "OK!100%";
    /**
     * minimum legth of caption
     */
    public static final int MIN_CAPTION_SIZE = 5;
    /**
     * deafault name of root folder
     */
    public static final String DEFAULT_ROOT_FOLDER = "HMB";
}
