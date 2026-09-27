// 曲情報(オブジェクト)を受け取り、隠しフィールドと選択表示に反映して画面を切り替える
// appleMusicUrlは画面には表示しないが、隠しフィールドとして送信データに含める
function selectSong(track) {
    document.getElementById("input-track-id").value = track.trackId;
    document.getElementById("input-track-name").value = track.trackName;
    document.getElementById("input-artist-name").value = track.artistName;
    document.getElementById("input-artwork-url").value = track.artworkUrl || "";
    document.getElementById("input-preview-url").value = track.previewUrl || "";
    document.getElementById("input-apple-music-url").value = track.appleMusicUrl || "";

    document.getElementById("selected-artwork").src = track.artworkUrl || "";
    document.getElementById("selected-track-name").textContent = track.trackName;
    document.getElementById("selected-artist-name").textContent = track.artistName;

    document.getElementById("song-browse-section").style.display = "none";
    document.getElementById("selected-section").style.display = "block";
}

// 「曲を選び直す」ボタン: 選択エリアを隠して、楽曲選択エリアに戻す
// (検索結果 or ランキングのどちらを表示していたかはそのまま維持される)
function backToBrowse() {
    document.getElementById("selected-section").style.display = "none";
    document.getElementById("song-browse-section").style.display = "block";
}

// 曲リスト(JSON配列)をカードとして #song-list-section に描画する
// (ランキング表示中の内容を、検索結果でまるごと置き換える形になる)
function renderSongList(tracks) {
    const container = document.getElementById("song-list-section");
    container.innerHTML = "";

    if (tracks.length === 0) {
        container.textContent = "該当する曲が見つかりませんでした。";
        return;
    }

    tracks.forEach(function (track) {
        const card = document.createElement("div");
        card.className = "song-card";
        card.addEventListener("click", function () {
            selectSong(track);
        });

        const img = document.createElement("img");
        img.src = track.artworkUrl || "";
        card.appendChild(img);

        const name = document.createElement("p");
        name.textContent = track.trackName;
        card.appendChild(name);

        const artist = document.createElement("p");
        artist.className = "artist";
        artist.textContent = track.artistName;
        card.appendChild(artist);

        container.appendChild(card);
    });
}

// 検索ボタン: /api/songs/search をJSON(fetch)で呼び出し、
// 「人気ランキング」が表示されていた場所を検索結果に差し替える
document.getElementById("search-button").addEventListener("click", function () {
    const keyword = document.getElementById("search-keyword").value.trim();
    if (!keyword) {
        return;
    }

    fetch("/api/songs/search?keyword=" + encodeURIComponent(keyword))
        .then(function (response) {
            if (!response.ok) {
                throw new Error("検索に失敗しました");
            }
            return response.json();
        })
        .then(function (tracks) {
            document.getElementById("song-list-heading").textContent = "検索結果";
            renderSongList(tracks);
        })
        .catch(function (error) {
            console.error(error);
            alert("曲の検索に失敗しました。時間をおいて再度お試しください。");
        });
});

// ページ読み込み後、自動的にランキングを取得して表示する。
// ランキングは補助的な表示なので、ページ自体の表示はこれを待たずに完了させ、
// 取得でき次第「読み込み中...」の表示と差し替える。
// (検索を実行済みの場合にランキングで上書きしないよう、まだ検索していない時だけ差し替える)
document.addEventListener("DOMContentLoaded", function () {
    fetch("/api/songs/ranking")
        .then(function (response) {
            if (!response.ok) {
                throw new Error("ランキングの取得に失敗しました");
            }
            return response.json();
        })
        .then(function (tracks) {
            // まだ検索を実行していない(見出しが「人気ランキング」のまま)場合のみ差し替える
            if (document.getElementById("song-list-heading").textContent === "人気ランキング") {
                renderSongList(tracks);
            }
        })
        .catch(function (error) {
            // ランキングは補助的な表示のため、失敗してもエラーダイアログは出さず、
            // 読み込み中の表示をメッセージに差し替えるだけにとどめる
            console.error(error);
            const loading = document.getElementById("song-list-loading");
            if (loading) {
                loading.textContent = "ランキングを取得できませんでした。";
            }
        });
});