# おとログ（仮）

「いま、何聴いてる？」を身近な人と共有する音楽SNSです。
音楽を検索して投稿したり、気分タグを付けたり、フレンドの投稿やリアクションを通知で受け取ったりできます。
ポートフォリオ作品として制作しています。

## 概要

おとログは、音楽を通じたコミュニケーションを目的としたWebアプリケーションです。
AppleのiTunes Search APIを利用して楽曲を検索でき、選択した楽曲に気分タグを付けて投稿できます。スマートフォンでの利用を想定し、PWAにも対応しています。

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
- PWA

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