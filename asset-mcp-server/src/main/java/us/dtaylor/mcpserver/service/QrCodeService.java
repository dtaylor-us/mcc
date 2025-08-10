package us.dtaylor.mcpserver.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for generating QR code images.  Uses ZXing to create
 * 300x300 PNG files.  Callers are responsible for moving or storing the
 * temporary file produced by this service.
 */
@Service
public class QrCodeService {

    /**
     * Generates a QR code image for the given text.  The image is written to
     * the provided output file path.  If necessary parent directories will be
     * created.  The file format is PNG.
     *
     * @param text      the text to encode in the QR code
     * @param outputFile the file on disk where the QR image will be written
     * @return the path to the written file
     * @throws Exception if QR code generation or writing fails
     */
    public Path generatePng(String text, Path outputFile) throws Exception {
        Files.createDirectories(outputFile.getParent());
        var writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 1);
        var matrix = writer.encode(text, BarcodeFormat.QR_CODE, 300, 300, hints);
        MatrixToImageWriter.writeToPath(matrix, "PNG", outputFile);
        return outputFile;
    }
}
