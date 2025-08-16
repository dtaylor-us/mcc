
import React, { useRef, useEffect, useState } from 'react';
import { CameraIcon, QrCodeIcon } from './icons';

interface QrScannerProps {
  onScan: (data: string) => void;
  onError: (error: string) => void;
}

const QrScanner: React.FC<QrScannerProps> = ({ onScan, onError }) => {
  const videoRef = useRef<HTMLVideoElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [isScanning, setIsScanning] = useState(false);
  const [stream, setStream] = useState<MediaStream | null>(null);

  useEffect(() => {
    let animationFrameId: number;

    const tick = () => {
      if (videoRef.current && videoRef.current.readyState === videoRef.current.HAVE_ENOUGH_DATA && canvasRef.current) {
        const canvas = canvasRef.current;
        const video = videoRef.current;
        const ctx = canvas.getContext('2d');

        if (ctx) {
          canvas.height = video.videoHeight;
          canvas.width = video.videoWidth;
          ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
          const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
          
          // jsQR is loaded from CDN
          const code = (window as any).jsQR(imageData.data, imageData.width, imageData.height, {
            inversionAttempts: 'dontInvert',
          });

          if (code) {
            onScan(code.data);
            stopScan();
          }
        }
      }
      animationFrameId = requestAnimationFrame(tick);
    };
    
    if (isScanning && stream) {
        animationFrameId = requestAnimationFrame(tick);
    }

    return () => {
      cancelAnimationFrame(animationFrameId);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isScanning, onScan, stream]);

  const startScan = async () => {
    try {
      const mediaStream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } });
      setStream(mediaStream);
      if (videoRef.current) {
        videoRef.current.srcObject = mediaStream;
      }
      setIsScanning(true);
    } catch (err) {
      console.error("Camera access error:", err);
      onError("Could not access camera. Please check permissions.");
    }
  };

  const stopScan = () => {
    if (stream) {
      stream.getTracks().forEach(track => track.stop());
    }
    setStream(null);
    setIsScanning(false);
  };

  return (
    <div className="p-4 border-2 border-dashed border-slate-600 rounded-lg text-center bg-slate-800">
      {!isScanning ? (
        <div className="flex flex-col items-center gap-4">
          <QrCodeIcon className="h-16 w-16 text-slate-400" />
          <button
            onClick={startScan}
            className="flex items-center gap-2 bg-cyan-600 hover:bg-cyan-500 text-white font-bold py-2 px-4 rounded-lg transition-colors"
          >
            <CameraIcon className="h-5 w-5" />
            Scan Asset QR
          </button>
        </div>
      ) : (
        <div className="relative">
          <video ref={videoRef} autoPlay playsInline className="w-full h-auto max-h-96 rounded-md" />
          <canvas ref={canvasRef} className="hidden" />
          <button
            onClick={stopScan}
            className="mt-4 bg-red-600 hover:bg-red-500 text-white font-bold py-2 px-4 rounded-lg transition-colors"
          >
            Stop Scanning
          </button>
        </div>
      )}
    </div>
  );
};

export default QrScanner;
