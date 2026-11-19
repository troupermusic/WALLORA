import { useEffect, useState } from 'react'

const slides = [
  {
    title: <>Explore 4K<br />Wallpapers</>,
    description: 'Curated ultra-HD visuals crafted for clarity and depth.',
    artwork: 'silk'
  },
  {
    title: <>Pure AMOLED<br />Deep Blacks</>,
    description: 'Crisp contrast and deep blacks made for OLED screens.',
    artwork: 'eclipse'
  },
  {
    title: <>One-Tap<br />Seamless Fit</>,
    description: 'Discover beautiful backgrounds for your home and lock screen.',
    artwork: 'mountain'
  }
]

export default function WalloraWelcome({ onStartExplore }) {
  const [active, setActive] = useState(0)
  const [splashDone, setSplashDone] = useState(false)

  useEffect(() => {
    const timer = setTimeout(() => setSplashDone(true), 1100)
    return () => clearTimeout(timer)
  }, [])

  return (
    <div className="wallora-welcome">
      <style>{`
        .wallora-welcome, .wallora-welcome * {
          box-sizing: border-box;
        }
        .wallora-welcome {
          position: fixed; inset: 0; z-index: 9999;
          background: #000; color: #fff;
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
          overflow: auto; display: flex; justify-content: center;
        }
        .ww-stage {
          width: 100%; max-width: 480px; min-height: 100%;
          padding: max(env(safe-area-inset-top), 25px) 24px
            max(env(safe-area-inset-bottom), 28px);
          display: flex; flex-direction: column;
          justify-content: space-between; gap: 20px;
          opacity: 0; transform: translateY(12px);
          transition: .7s ease;
        }
        .ww-stage.visible { opacity: 1; transform: none; }
        .ww-header { display:flex; align-items:center; justify-content:space-between; }
        .ww-brand { display:flex; align-items:center; gap:11px; font-size:14px; font-weight:800; letter-spacing:3px; }
        .ww-mark { width:34px; height:34px; display:grid; place-items:center;
          border:1px solid #444; border-radius:11px; background:#171717; letter-spacing:0; }
        .ww-label { font-size:10px; letter-spacing:2px; color:#888; }
        .ww-art {
          height: min(39vh, 340px); min-height:250px;
          display:flex; align-items:center; justify-content:center; position:relative;
        }
        .ww-card {
          position:absolute; overflow:hidden; border:1px solid #383838;
          border-radius:25px; box-shadow:0 20px 65px #000;
        }
        .ww-left, .ww-right { width:clamp(125px,34vw,155px); height:75%; opacity:.75; }
        .ww-left { left:2%; transform:rotate(-9deg); background:radial-gradient(ellipse at 40% 40%,#777,#202020 50%,#050505); }
        .ww-right { right:2%; transform:rotate(9deg); background:linear-gradient(150deg,#555,#161616 45%,#020202); }
        .ww-center { z-index:2; width:clamp(170px,47vw,205px); height:95%;
          background:radial-gradient(circle at 50% 42%,#aaa 0%,#555 9%,#191919 28%,#050505 65%);
          display:flex; align-items:flex-end; justify-content:center; padding-bottom:17px; }
        .ww-center.silk { background:radial-gradient(ellipse at 50% 45%,#aaa,#303030 25%,#080808 70%); }
        .ww-center.mountain { background:linear-gradient(165deg,#252525,#090909 55%,#000); }
        .ww-center.mountain:after { content:''; position:absolute; inset:45% 0 0;
          background:linear-gradient(145deg,transparent 25%,#777 26%,#222 48%,transparent 49%),
          linear-gradient(35deg,#080808 35%,#555 36%,#111 65%); }
        .ww-center.eclipse { background:radial-gradient(circle at 50% 43%,#eee 0%,#777 12%,#222 26%,#000 65%); }
        .ww-art-tag { z-index:2; padding:7px 12px; border-radius:30px;
          background:#171717dd; border:1px solid #555; font-size:10px; letter-spacing:1px; }
        .ww-bottom { text-align:center; padding-bottom:8px; }
        .ww-dots { display:flex; justify-content:center; gap:8px; margin-bottom:22px; }
        .ww-dot { width:7px; height:6px; padding:0; border:0; border-radius:9px;
          background:#555; transition:.25s; }
        .ww-dot.active { width:25px; background:#fff; }
        .ww-title { font-size:clamp(29px,8vw,36px); line-height:1.12;
          letter-spacing:-1px; font-weight:850; margin:0 0 13px; }
        .ww-description { color:#999; font-size:14px; line-height:1.6;
          max-width:290px; margin:0 auto 27px; }
        .ww-button { height:58px; width:100%; border:0; border-radius:40px;
          background:#fff; color:#000; font-size:16px; font-weight:800;
          position:relative; cursor:pointer; }
        .ww-arrow { position:absolute; right:8px; top:8px; width:42px; height:42px;
          display:grid; place-items:center; border-radius:50%; background:#000;
          color:#fff; font-size:21px; }
        .ww-splash { position:fixed; inset:0; z-index:5; background:#000;
          display:grid; place-items:center; transition:opacity .4s, visibility .4s; }
        .ww-splash.hidden { opacity:0; visibility:hidden; pointer-events:none; }
        .ww-splash-inner { text-align:center; animation:ww-enter .7s ease both; }
        .ww-splash-mark { width:76px; height:76px; display:grid; place-items:center;
          margin:auto auto 18px; border:1px solid #555; border-radius:24px;
          background:#171717; font-size:34px; font-weight:900; }
        .ww-splash-name { font-size:19px; font-weight:900; letter-spacing:6px; }
        .ww-loader { height:2px; width:100px; background:#333; margin:20px auto; overflow:hidden; }
        .ww-loader:after { content:''; display:block; height:100%; width:40%;
          background:#fff; animation:ww-load .9s ease forwards; }
        @keyframes ww-load { to { transform:translateX(250%); } }
        @keyframes ww-enter { from { opacity:0; transform:scale(.85); } to { opacity:1; transform:scale(1); } }
        @media (max-height:650px) {
          .ww-art { min-height:190px; height:30vh; }
          .ww-bottom { padding-bottom:0; }
          .ww-description { margin-bottom:15px; }
        }
      `}</style>

      <main className={`ww-stage ${splashDone ? 'visible' : ''}`}>
        <header className="ww-header">
          <div className="ww-brand"><span className="ww-mark">W</span> WALLORA</div>
          <span className="ww-label">4K ULTRA</span>
        </header>

        <section className="ww-art" aria-label="Wallpaper previews">
          <div className="ww-card ww-left" />
          <div className="ww-card ww-right" />
          <div className={`ww-card ww-center ${slides[active].artwork}`}>
            <span className="ww-art-tag">
              {['LIQUID SILK', 'ECLIPSE HORIZON', 'SILENT RIDGE'][active]}
            </span>
          </div>
        </section>

        <footer className="ww-bottom">
          <div className="ww-dots">
            {slides.map((slide, index) => (
              <button key={index} type="button"
                className={`ww-dot ${active === index ? 'active' : ''}`}
                aria-label={`Slide ${index + 1}`}
                onClick={() => setActive(index)} />
            ))}
          </div>
          <h1 className="ww-title">{slides[active].title}</h1>
          <p className="ww-description">{slides[active].description}</p>
          <button className="ww-button" type="button" onClick={onStartExplore}>
            Start Explore <span className="ww-arrow">→</span>
          </button>
        </footer>
      </main>

      <div className={`ww-splash ${splashDone ? 'hidden' : ''}`}>
        <div className="ww-splash-inner">
          <div className="ww-splash-mark">W</div>
          <div className="ww-splash-name">WALLORA</div>
          <div className="ww-loader" />
        </div>
      </div>
    </div>
  )
}
