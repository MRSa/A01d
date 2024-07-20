# A01d

Android application for taking pictures with various digital cameras via WiFi.

This document is written in Japanese.

-----------

# A01d : OlympusAirを望遠鏡として使ってみおうとしたら、WiFi経由で様々なデジタルカメラの撮影をするための Androidアプリになっちゃった版

--------------------------------

## 概要

A01dは、ライブビュー拡大表示機能を利用した望遠鏡アプリケーションです。 Olympus Camera Kitを使用したオープンプラットフォームカメラ OLYMPUS AIR A01 に対応しています。
最近の版では、SONY, RICOH/PENTAX, FUJIFILX X, PANASONIC, KODAX PIXPRO, Canon, NIKONといった、各メーカーのWIFI接続カメラの制御に対応しました。ただし、撮影の制御は、OLYMPUS AIR と比較し操作の制約がありますのでご了承ください。
Olympus Airでは、ライブビューの拡大機能を使うことで、カメラで撮影するときのサイズから最大14倍のサイズで表示させることができます。画像は粗くなるとはいえ、**その場で見る** だけであれば、遠くの様子を観察することができます。
1.8.0 から、タイムシフトと呼べる、画像の内部に溜め、表示を少し遅らせて表示させる機能を搭載しました。これにより、数秒前の撮影時動作を確認する、ということができるようになりました。

-----------

## 制御対象カメラ

- [**OLYMPUS AIR A01**](https://jp.omsystem.com/cms/record/dslr/a01/index.pdf)
  - [「OLYMPUS AIR A01」 は 2018年 3月 31日をもって販売を終了いたしました。](https://digital-faq.jp.omsystem.com/faq/public/app/servlet/relatedqa?QID=005796)
- OMDS(OLYMPUS) OM-D / PEN シリーズ / TGシリーズ
- NIKON
- CANON
- Fujifilm
- Panasonic
- Sony
- JK Imaging KODAK PIXPRO WPZ2

## 操作説明

- [操作説明](https://github.com/MRSa/GokigenOSDN_documents/blob/main/Applications/A01d/Readme.md)

## OlympusCameraKitについて

A01d は、OlympusCameraKit を使用してOLYMPUS AIR A01と通信を行います。そのため、以下の「SDKダウンロード許諾契約書」の条件に従います。

- [EULA_OlympusCameraKit_ForDevelopers_jp.pdf](https://github.com/MRSa/gokigen/blob/5ec908fdbe16c4de9e37fe90d70edc9352b6f948/osdn-svn/Documentations/miscellaneous/EULA_OlympusCameraKit_ForDevelopers_jp.pdf)

-----------
