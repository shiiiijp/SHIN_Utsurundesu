# シン・写ルンです

> **Warning**  
> `CONNECT TO CAMERA` ボタンを押すとクラッシュします。
> 現時点でBluetoothアドレスがplaceholderであるためです。

プレビュー画像のリクエスト用 string は `String request = "previewRequest"`<br>
撮影のリクエスト用 string は `String request = "shootRequest"` です。<br><br>
Raspberry Pi (Python) 側の実装は以下のようなものを想定しています。<br>
(ChatGPTより作成)

```python
import bluetooth

# Bluetoothソケットの待ち受けとデータの受信処理
def receive_request():
    # Raspberry PiのBluetoothアドレス
    raspberry_pi_address = "Raspberry PiのBluetoothアドレス"
    # UUID（上記のAndroid側と同じUUIDを使用）
    port_uuid = "UUID HERE"

    # Bluetoothソケットの作成
    server_sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    server_sock.bind((raspberry_pi_address, bluetooth.PORT_ANY))
    server_sock.listen(1)

    # Android側からの接続を待ち受ける
    client_sock, client_info = server_sock.accept()
    print("接続完了:", client_info)

    try:
        # データの受信
        request_data = client_sock.recv(1024)
        request = request_data.decode("utf-8")

        # 受信したリクエストに応じた処理を実行
        if request == "写真のリクエスト":  # 実際のリクエスト内容に応じて適宜変更してください
            # 写真の取得処理を実行
            # ...

    except Exception as e:
        print("エラー:", str(e))

    # ソケットのクローズ
    client_sock.close()
    server_sock.close()
```
