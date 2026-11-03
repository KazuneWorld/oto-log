# おとログ（仮）

「いま、何聴いてる？どんな曲の気分？」を、身近な人とだけ共有する音楽SNSです。

文字は書きません。聴いている曲を選んで、今の気分を表すタグを1つ付けるだけ。フレンドは、その曲をタイムラインで見て、スタンプ（絵文字）で気軽に反応します。

ポートフォリオ作品として制作しています。

## 概要

おとログは、友人・恋人・家族など、親しい人だけがつながるクローズドな音楽SNSです。

一般的なSNSのように、不特定多数に向けて発信したり、フォロワー数や「いいね」の数を気にしたりする必要はありません。長い文章を考える必要もありません。聴いた曲と気分だけを、親しい人にそっと共有する「ゆるさ」を大切にしています。

曲の検索・ランキング・試聴には、AppleのiTunes Search APIとApple Music RSS Feed APIを利用しています。スマートフォンの縦画面での利用を前提に、PWA（ホーム画面に追加して、アプリのように使える仕組み）にも対応しています。

### こんな場面を想定しています

- 友達が今どんな曲を聴いているか、なんとなく知りたい
- SNS疲れをせずに、親しい人とゆるくつながりたい
- 言葉にするより、曲で今の気持ちを伝えたい

## 主な機能

### 実装済み・実装中の機能

- ユーザー登録
- メールアドレスまたはログインIDによるログイン
- BCryptによるパスワードハッシュ化
- Spring Securityによる認証・認可
- 楽曲検索
- Apple Music楽曲ランキングの取得
- 楽曲試聴
- 気分タグを付けた楽曲投稿
- 通知機能の基盤
  - フレンド申請
  - フレンド承認
  - 投稿へのリアクション
  - フレンドの新規投稿
- 通知の未読管理
- PWA対応
- PWAプッシュ通知ロジック（デプロイ時までコメントアウト）
- オフライン画面

### 今後実装予定の機能

現在、以下の機能は画面または一部のバックエンドのみ実装されています。

- タイムライン表示
- 投稿詳細画面
- リアクション操作
- ブックマーク機能
- フレンド検索・申請・承認・削除
- フレンド一覧
- QRコードを利用したフレンド追加
- ユーザー情報変更
- プロフィール画面
- テーマ変更
- 投稿回数の制限（午前・午後に1回ずつ）

### 機能の詳細

#### 楽曲の検索・ランキング・試聴

- 曲名やアーティスト名で、iTunes Search APIから楽曲を検索できます。
- 投稿画面を開くと、Apple Musicの人気楽曲ランキング（トップ10）が表示されます。
- 各曲には試聴プレイヤーが付いていて、30秒程度のプレビューを聴けます。
- 曲を選ぶと、同じ画面の中で「選んだ曲・気分タグの選択・投稿ボタン」の表示に切り替わります。

#### 気分タグ

- 「リラックス」「元気」「悲しい」「チル」などの種類から、投稿ごとに1つ選びます。
- タグはマスタテーブルで管理していて、追加する場合は、コードを変更せずデータを足すだけで済みます。

#### 投稿と曲のデータ

- 投稿には、曲と気分タグだけを保存します。曲の情報（曲名、アーティスト名、アートワーク、試聴URL、Apple MusicのURL）は`songs`テーブルに保存し、同じ曲は1行を使い回す設計です（iTunesの曲IDで判定）。
- 投稿の回数は、午前と午後で1回ずつに制限する予定です。

#### フレンド

- フレンドコードは、8文字のランダムな英数字で、システムが自動で生成します。検索は**完全一致のみ**で、部分一致で他人を探すことはできません。
- ログインIDは自由に決められる「ログイン専用」の値で、フレンドコードとは別です。ログインIDで検索されて、見知らぬ人から申請が届く、といったことが起きないようにしています。
- 申請を却下した場合は、レコードを削除して、申請前の状態に戻します。

#### リアクション・ブックマーク

- リアクションは、絵文字ピッカーで選んだ任意の絵文字を、そのまま文字列として保存します。同じ投稿には、1人1つまで。押し直すと、別の絵文字に変わります。
- ブックマークした投稿は、一覧画面でまとめて見返せます。

#### 通知

- 未読の件数は、フッターの「通知」リンクにバッジで表示します。
- 通知一覧を開くと、表示した分がまとめて既読になります。開いた直後の表示では、未読だったものを強調します。
- 同じ内容の未読通知がすでにある間は、重複して作りません（例：リアクションの絵文字を何度押し直しても、通知は1件のままです）。

通知を作る処理は、`NotificationService`に集約しています。各機能は、通知を作りたい時にそのメソッドを1行呼ぶだけで、上記のルールが共通で適用されます。

#### PWA

- Web App ManifestとService Workerにより、ホーム画面への追加、全画面表示、オフライン時の案内画面に対応しています。
- ログインが必要で、画面の内容がユーザーごとに変わるため、Service Workerは画面やAPIのレスポンスをキャッシュしません（古い内容や他人の内容を表示しないため）。

## 使用技術

### バックエンド

- Java 21
- Spring Boot 4.1.1
- Spring MVC
- Spring Security
- MyBatis
- Lombok
- Maven

### フロントエンド

- Thymeleaf
- HTML
- CSS
- JavaScript
- PWA（Web App Manifest・Service Worker・Web Push）

### データベース

- MySQL
- H2 Database（開発・テスト用）

### 外部API

- iTunes Search API
- Apple Music RSS Feed API

## プロジェクト構成

```text
src/
├─ main/
│  ├─ java/com/example/webapp/
│  │  ├─ api/          外部API連携・API Controller
│  │  ├─ config/       Spring設定・Security設定
│  │  ├─ controller/   画面Controller
│  │  ├─ entity/       データモデル
│  │  ├─ mapper/       MyBatis Mapper
│  │  └─ service/      アプリケーションロジック
│  │
│  └─ resources/
│     ├─ mapper/       MyBatis XML
│     ├─ templates/    Thymeleafテンプレート
│     ├─ static/       JavaScript・画像・PWA関連ファイル
│     ├─ schema.sql    データベース定義
│     └─ data.sql      開発用初期データ
│
└─ test/
   └─ java/             テストコード
```

## 動作環境

- Java 21
- Maven
- MySQL 8.x
- Docker / Docker Compose（コンテナで起動する場合）
- Git

## セットアップ

### 1. リポジトリを取得

```bash
git clone <repository-url>
cd oto-log
```

### 2. データベースを作成

MySQLに `otolog` データベースを作成します。

```sql
CREATE DATABASE otolog
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 3. データベース接続設定

`src/main/resources/application.properties` を環境に合わせて設定します。

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/otolog?serverTimezone=Asia/Tokyo
spring.datasource.username=root
spring.datasource.password=your-password
```


### 4. Docker Composeで起動する場合

Dockerがインストールされている環境では、プロジェクトルートで以下を実行できます。

```bash
docker compose up --build
```

アプリケーションは `http://localhost:8080/`、MySQLは `localhost:3306` で起動します。

`docker-compose.yml` に記載されているデータベースのパスワードは開発用です。本番環境では環境変数やAWS Secrets Managerなどで管理してください。

### 5. Mavenで起動する場合

Dockerを使わず、ローカルのMySQLへ接続して起動する場合は以下を実行します。

Windowsの場合:

```bat
mvnw.cmd spring-boot:run
```

macOS・Linuxの場合:

```bash
./mvnw spring-boot:run
```

## PWAプッシュ通知

PWAプッシュ通知の送受信ロジックは実装済みです。ただし、開発中の画面構成と混同しないよう、設定画面のプッシュ通知UIは現在コメントアウトしています。

利用可能にする場合は、設定画面のコメントアウトを解除したうえで、VAPID鍵を環境変数に設定します。

```text
PUSH_VAPID_PUBLIC_KEY=...
PUSH_VAPID_PRIVATE_KEY=...
PUSH_VAPID_SUBJECT=mailto:admin@example.com
```

秘密鍵はリポジトリへコミットしないでください。プッシュ通知はHTTPS環境（またはlocalhost）と、通知を許可したブラウザ・端末が必要です。

### 6. 起動後の確認

起動後、以下のURLにアクセスします。

```text
http://localhost:8080/
```

## 設計上の特徴

### Spring Securityによる認証

ログイン処理にはSpring Securityを利用しています。パスワードはBCryptでハッシュ化し、認証済みユーザーのみが必要な画面へアクセスできる構成にしています。

### MyBatisによるSQL管理

データベースアクセスにはMyBatisを利用しています。SQLをXMLファイルで管理することで、複雑な検索処理やJOINを明示的に記述できるようにしています。

### 通知処理の集約

通知の作成処理を `NotificationService` に集約しています。

これにより、通知の宛先、重複防止、自分自身への通知除外などのルールを各Controllerで重複して実装しない構成にしています。

### 外部APIのキャッシュ

楽曲ランキングは外部APIへのアクセス回数を抑えるため、一定時間キャッシュする仕組みを採用しています。

## デプロイ計画

DockerfileとDocker Composeによるコンテナ化用ファイルを追加済みです。現在は主にローカル開発・動作確認用として利用し、AWSへのデプロイ時に本番向け設定へ調整します。

AWS上では、以下の構成を検討しています。

- Amazon ECR：Dockerイメージの保存
- Amazon ECS：コンテナの実行
- Amazon RDS：本番データベース
- Amazon S3：ユーザーアイコンの保存
- Application Load Balancer：外部からのHTTPSアクセス

## ポートフォリオとしての制作目的

この作品では、以下の技術や設計を学習・実践しています。

- Spring BootによるWebアプリケーション開発
- Spring Securityを利用した認証処理
- MyBatisによるデータベースアクセス
- 外部APIとの連携
- Thymeleafによるサーバーサイドレンダリング
- JavaScriptによる非同期通信
- PWAの基本的な構成
- 通知やフレンド関係など、複数テーブルにまたがる機能設計
- トランザクション管理
- 機能追加を意識したService層の設計

## 今後の改善予定

- DTOとEntityの分離
- 入力値バリデーションの強化
- テストコードの拡充
- AWSへのデプロイ
- UI・アクセシビリティの改善