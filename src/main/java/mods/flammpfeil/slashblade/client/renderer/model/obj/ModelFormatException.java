package mods.flammpfeil.slashblade.client.renderer.model.obj;

import java.io.Serial;

/**
 * Thrown if there is a problem parsing the model
 *
 * @author cpw
 */
public class ModelFormatException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2023547503969671835L;

    public ModelFormatException() {
        super();
    }

    public ModelFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelFormatException(String message) {
        super(message);
    }

    public ModelFormatException(Throwable cause) {
        super(cause);
    }

}
