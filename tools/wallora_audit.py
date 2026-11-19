from pathlib import Path
from PIL import Image, ImageDraw
import csv

src = Path("/storage/emulated/0/wallora pics")
out = Path.home() / "WALLORA" / "audit"
out.mkdir(parents=True, exist_ok=True)

files = sorted(p for p in src.iterdir() if p.is_file())
images = [p for p in files if p.suffix.lower() in {".webp",".jpg",".jpeg",".png",".heic"}]
videos = [p for p in files if p.suffix.lower() in {".mp4",".mov"}]

thumbs = []
rows = []
errors = []

for i, p in enumerate(images, 1):
    try:
        im = Image.open(p).convert("RGB")
        w, h = im.size
        im.thumbnail((170, 235))
        tile = Image.new("RGB", (190, 285), "#161616")
        tile.paste(im, ((190-im.width)//2, 8))
        d = ImageDraw.Draw(tile)
        d.text((8, 250), f"#{i}  {w}x{h}", fill="white")
        d.text((8, 266), p.name[:25], fill="#aaaaaa")
        thumbs.append((i, tile))
        rows.append([i, p.name, p.suffix.lower(), w, h, p.stat().st_size])
    except Exception as e:
        errors.append([p.name, str(e)])

with open(out/"image_inventory.csv", "w", newline="") as f:
    wr = csv.writer(f)
    wr.writerow(["number","filename","format","width","height","bytes"])
    wr.writerows(rows)

with open(out/"unreadable_images.csv", "w", newline="") as f:
    wr = csv.writer(f)
    wr.writerow(["filename","error"])
    wr.writerows(errors)

for start in range(0, len(thumbs), 40):
    batch = thumbs[start:start+40]
    sheet = Image.new("RGB", (190*4, 285*10), "#080808")
    for j, (num, tile) in enumerate(batch):
        sheet.paste(tile, ((j%4)*190, (j//4)*285))
    sheet.save(out/f"contact_{start//40+1}.jpg", quality=90)

print(f"Images processed: {len(thumbs)} / {len(images)}")
print(f"Videos found: {len(videos)}")
print(f"Unreadable images: {len(errors)}")
print(f"Audit folder: {out}")
print("Contact sheets:", ", ".join(p.name for p in sorted(out.glob("contact_*.jpg"))))
