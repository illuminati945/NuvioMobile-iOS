import Foundation
import AVFoundation
import UIKit
import UserNotifications
import AudioToolbox

private let downloadsKeepAliveStartNotification = Notification.Name("NuvioDownloadsKeepAliveStart")
private let downloadsKeepAliveStopNotification = Notification.Name("NuvioDownloadsKeepAliveStop")
private let downloadsPostNotificationName = Notification.Name("NuvioDownloadsPostNotification")
private let downloadsOpenPickerNotification = Notification.Name("NuvioDownloadsOpenFolderPicker")

final class DownloadsBackgroundKeepAlive: NSObject, UIDocumentPickerDelegate {
    static let shared = DownloadsBackgroundKeepAlive()

    private var activeDownloadsCount = 0
    private var audioPlayer: AVAudioPlayer?
    private var observers: [NSObjectProtocol] = []

    private override init() {
        super.init()
    }

    func start() {
        guard observers.isEmpty else { return }

        let center = NotificationCenter.default
        observers.append(
            center.addObserver(forName: downloadsKeepAliveStartNotification, object: nil, queue: .main) { [weak self] _ in
                self?.increment()
            }
        )
        observers.append(
            center.addObserver(forName: downloadsKeepAliveStopNotification, object: nil, queue: .main) { [weak self] _ in
                self?.decrement()
            }
        )
        observers.append(
            center.addObserver(forName: downloadsPostNotificationName, object: nil, queue: .main) { [weak self] notification in
                guard let userInfo = notification.userInfo as? [String: String] else { return }
                let title = userInfo["title"] ?? "Nuvio Download"
                let body = userInfo["body"] ?? ""
                let isSuccess = userInfo["success"] != "0"
                self?.postNotification(title: title, body: body, isSuccess: isSuccess)
            }
        )
    }

    private func increment() {
        activeDownloadsCount += 1
        if activeDownloadsCount == 1 {
            startSilentAudio()
        }
    }

    private func decrement() {
        activeDownloadsCount = max(0, activeDownloadsCount - 1)
        if activeDownloadsCount == 0 {
            stopSilentAudio()
        }
    }

    private func startSilentAudio() {
        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.playback, mode: .default, options: [.mixWithOthers])
            try session.setActive(true)

            if audioPlayer == nil {
                let silentData = createSilentWavData()
                let player = try AVAudioPlayer(data: silentData)
                player.numberOfLoops = -1
                player.volume = 0.0
                player.prepareToPlay()
                self.audioPlayer = player
            }
            audioPlayer?.play()
            print("[Nuvio] Background audio keep-alive started")
        } catch {
            print("[Nuvio] Failed to start audio keep-alive: \(error.localizedDescription)")
        }
    }

    private func stopSilentAudio() {
        audioPlayer?.stop()
        audioPlayer = nil
        do {
            try AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
            print("[Nuvio] Background audio keep-alive stopped")
        } catch {
            print("[Nuvio] Failed to deactivate audio session: \(error.localizedDescription)")
        }
    }

    private func postNotification(title: String, body: String, isSuccess: Bool) {
        // Haptic feedback
        let feedback = UINotificationFeedbackGenerator()
        feedback.prepare()
        if isSuccess {
            feedback.notificationOccurred(.success)
            AudioServicesPlaySystemSound(1007)
        } else {
            feedback.notificationOccurred(.error)
            AudioServicesPlaySystemSound(1073)
        }

        // Local Notification banner
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default

        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 0.1, repeats: false)
        let request = UNNotificationRequest(
            identifier: "nuvio.download.\(Date().timeIntervalSince1970)",
            content: content,
            trigger: trigger
        )
        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("[Nuvio] Notification add error: \(error.localizedDescription)")
            }
        }
    }

    private func int16ToBytes(_ value: Int16) -> [UInt8] {
        let u = UInt16(bitPattern: value)
        return [UInt8(u & 0xFF), UInt8((u >> 8) & 0xFF)]
    }

    private func int32ToBytes(_ value: Int32) -> [UInt8] {
        let u = UInt32(bitPattern: value)
        return [
            UInt8(u & 0xFF),
            UInt8((u >> 8) & 0xFF),
            UInt8((u >> 16) & 0xFF),
            UInt8((u >> 24) & 0xFF)
        ]
    }

    private func createSilentWavData() -> Data {
        let sampleRate: Int32 = 8000
        let numSamples: Int32 = 8000 // 1 second
        let dataSize = numSamples
        let totalSize: Int32 = 36 + dataSize
        var data = Data()

        // RIFF
        data.append(contentsOf: [0x52, 0x49, 0x46, 0x46]) // "RIFF"
        data.append(contentsOf: int32ToBytes(totalSize))
        data.append(contentsOf: [0x57, 0x41, 0x56, 0x45]) // "WAVE"

        // fmt 
        data.append(contentsOf: [0x66, 0x6D, 0x74, 0x20]) // "fmt "
        let subchunk1Size: Int32 = 16
        data.append(contentsOf: int32ToBytes(subchunk1Size))
        let audioFormat: Int16 = 1 // PCM
        data.append(contentsOf: int16ToBytes(audioFormat))
        let numChannels: Int16 = 1 // Mono
        data.append(contentsOf: int16ToBytes(numChannels))
        data.append(contentsOf: int32ToBytes(sampleRate))
        let byteRate: Int32 = sampleRate * 1 * 1
        data.append(contentsOf: int32ToBytes(byteRate))
        let blockAlign: Int16 = 1
        data.append(contentsOf: int16ToBytes(blockAlign))
        let bitsPerSample: Int16 = 8
        data.append(contentsOf: int16ToBytes(bitsPerSample))

        // data
        data.append(contentsOf: [0x64, 0x61, 0x74, 0x61]) // "data"
        data.append(contentsOf: int32ToBytes(dataSize))
        data.append(contentsOf: [UInt8](repeating: 128, count: Int(dataSize)))

        return data
    }
}
