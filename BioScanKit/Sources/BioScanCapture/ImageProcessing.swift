import UIKit

public enum BioScanImageProcessing {
    public static func normalized(_ image: UIImage) -> UIImage {
        guard image.imageOrientation != .up else { return image }

        let format = UIGraphicsImageRendererFormat.default()
        format.scale = image.scale
        let renderer = UIGraphicsImageRenderer(size: image.size, format: format)
        return renderer.image { _ in
            image.draw(in: CGRect(origin: .zero, size: image.size))
        }
    }

    public static func crop(
        _ image: UIImage,
        normalizedRect: CGRect
    ) -> UIImage? {
        let normalizedImage = normalized(image)
        guard let cgImage = normalizedImage.cgImage else { return nil }

        let unitRect = normalizedRect.intersection(
            CGRect(x: 0, y: 0, width: 1, height: 1)
        )
        guard !unitRect.isNull, !unitRect.isEmpty else { return nil }

        let pixelRect = CGRect(
            x: unitRect.minX * CGFloat(cgImage.width),
            y: unitRect.minY * CGFloat(cgImage.height),
            width: unitRect.width * CGFloat(cgImage.width),
            height: unitRect.height * CGFloat(cgImage.height)
        ).integral

        guard let cropped = cgImage.cropping(to: pixelRect) else { return nil }
        return UIImage(
            cgImage: cropped,
            scale: normalizedImage.scale,
            orientation: .up
        )
    }
}
