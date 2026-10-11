// Service Workerを登録する(全ページ共通で読み込む)
//
// Service WorkerはHTTPS(または localhost)でしか動作しない。
// それ以外の環境では navigator.serviceWorker が存在しないため、何もせず終了する。
if ("serviceWorker" in navigator) {
    window.addEventListener("load", function () {
        // /sw.js をサイトのルートに置くことで、サイト全体(scope: "/")を管理できる
        navigator.serviceWorker.register("/sw.js").catch(function (error) {
            console.error("Service Workerの登録に失敗しました。", error);
        });
    });
}
