# PlanbookAI — Custom Design System Prompt


---

## MASTER DESIGN PROMPT

```
You are designing PlanbookAI — an AI-powered teaching tools portal for Vietnamese high school
Chemistry teachers. The platform has two distinct visual zones that must feel like one product:

ZONE 1 — LANDING PAGE (dark, bold, marketing-forward)
ZONE 2 — APP / DASHBOARD (light, clean, productivity-focused)

---

IDENTITY & PERSONALITY

Product personality: "A trusted academic tool powered by cutting-edge AI."
NOT a startup. NOT a flashy consumer app. A serious, capable platform that respects teachers' time.
Visual keywords: Structured. Trustworthy. Efficient. Modern-academic.
Emotional goal: Teachers should feel empowered and organized, not overwhelmed.

---

COLOR SYSTEM

Primary Accent:    #0052FF  (Electric Blue — AI actions, highlights, CTAs)
Accent Light:      #4D7CFF  (gradient endpoint)
Accent Subtle:     rgba(0, 82, 255, 0.08)  (tinted backgrounds, active states)
Signature Gradient: linear-gradient(135deg, #0052FF, #4D7CFF)

Use gradient on: primary buttons, AI badges, active nav indicators, key stats, CTA sections.

LANDING PAGE (dark zone):
  Background:      #0A0F1E  (deep navy)
  Surface:         #111827  (card backgrounds)
  Surface Raised:  #1A2235  (elevated cards)
  Text Primary:    #F8FAFC
  Text Secondary:  rgba(248, 250, 252, 0.6)
  Border:          rgba(255,255,255,0.08)

APP / DASHBOARD (light zone):
  Background:      #F8FAFC  (warm off-white canvas)
  Surface:         #FFFFFF  (cards, panels)
  Surface Muted:   #F1F5F9  (sidebar, secondary areas)
  Text Primary:    #0F172A  (deep slate)
  Text Secondary:  #64748B
  Border:          #E2E8F0
  Success:         #10B981
  Warning:         #F59E0B
  Danger:          #EF4444

---

TYPOGRAPHY

Display (headings only):  "Plus Jakarta Sans", weight 700-800, tracking -0.02em
Body / UI:                "DM Sans", weight 400-500 — highly readable at 13-14px
Mono (AI labels, IDs):    "JetBrains Mono", weight 400

Use Plus Jakarta Sans for page titles and section headlines.
Use DM Sans for everything inside the app: labels, body, table cells, descriptions.
Use JetBrains Mono for: AI badges, question IDs, status codes, nav section labels.

---

LANDING PAGE (dark zone)

Hero Section:
- Dark canvas #0A0F1E with subtle dot grid texture:
  radial-gradient(circle, rgba(255,255,255,0.04) 1px, transparent 1px) 28px 28px
- One large ambient glow: radial gradient #0052FF at 6% opacity, top-right corner, blur 200px
- Left col (60%): Plus Jakarta Sans 700, 56px headline
  "AI-powered" phrase uses gradient text (background-clip: text)
  Subhead: DM Sans 18px, text-secondary, 28px below headline
  Two CTAs stacked: Primary gradient button + Ghost button (white border)
- Right col (40%): Floating UI mockup card
  Shows a mini exam question with AI badge and score bar
  Float animation: 6s ease-in-out infinite, y-axis ±12px
  Behind it: radial glow #0052FF at 8% opacity

Stats band below hero:
  4 metrics horizontal, dividers between them
  Numbers: Plus Jakarta Sans 700, 36px, gradient text
  Labels: DM Sans 14px, text-secondary
  Examples: "70% less grading time" / "10x faster exam creation"

AI Badge pattern (used throughout landing):
  Pill | border: rgba(0,82,255,0.3) | bg: rgba(0,82,255,0.08)
  Left dot: #0052FF solid 6px, pulse animation 2s infinite
  Text: JetBrains Mono 11px, UPPERCASE, tracking-widest, color #4D7CFF
  Examples: "● AI-POWERED"  "● GEMINI VISION"  "● AUTO-GRADING"

Feature rows (alternating left/right layout):
  Show 4 features: AI Exam Generation, Question Bank, OCR Grading, Analytics
  Each: gradient icon square (48px) + headline + 2-line description + mini UI screenshot card

Value comparison section:
  Table comparing PlanbookAI vs ChatGPT vs Manual work
  Header row uses gradient bg, checkmarks in #10B981, X marks in #EF4444

Landing motion:
  Hero float: 6s ease-in-out infinite, y ±12px
  AI badge pulse: 2s ease-in-out infinite, scale 1→1.4→1
  Section entrance: fadeInUp, y 24→0, opacity 0→1, 600ms, ease [0.16,1,0.3,1]
  Stagger children: 80ms delay between sibling elements

---

APP / DASHBOARD (light zone)

Shell layout:
  Fixed sidebar: 240px wide, white bg, right border 1px #E2E8F0
  Top navbar: 56px tall, white, bottom border 1px #E2E8F0
  Content area: #F8FAFC background, padding 24px

Sidebar:
  Logo at top (gradient accent dot beside text)
  Nav group labels: JetBrains Mono 10px, UPPERCASE, tracking-widest, slate-400
  Groups: MAIN (Dashboard, Lesson Plans) / CONTENT (Question Bank, Exams) /
          GRADING (OCR & Grading, Results) / INSIGHTS (Analytics)
  Active item: 3px gradient left border + accent-subtle bg + accent text color
  Bottom: user avatar + name + subscription tier badge

Dashboard (home):
  Greeting: "Good morning, Lan" Plus Jakarta Sans 600, 24px
  4 stat cards in a row:
    White card, rounded-xl, border, shadow-sm
    32px gradient square icon (rounded-lg)
    Number: Plus Jakarta Sans 700, 28px, slate-900
    Label: DM Sans 13px, slate-500
    Trend badge: green (+) or red (-) with arrow
  Quick Actions (3 large cards): "Generate Exam" / "Grade Papers" / "New Lesson Plan"
    Each: white card, gradient icon area top, hover shadow-lg + translateY(-2px)

Question Bank screen:
  Filter bar: subject dropdown + grade dropdown + search input + difficulty pills
  2-column card grid
  Each card: white, rounded-xl, border, shadow-sm
    Top: question type badge (MC/Short/Fill) + difficulty badge
    Difficulty: Easy=green, Medium=amber, Hard=red (10% opacity bg, full color text)
    Body: question text, 2-line truncate
    Footer: bank name chip + AI badge (gradient bg, white text, sparkle icon) + action icons

Exam Builder screen:
  Split layout: left 40% question picker, right 60% exam canvas
  Left: searchable question list with checkboxes, filters
  Right: drag-drop zone, question order list, settings panel
  Bottom: full-width gradient "Publish Exam" button

OCR Grading screen:
  Upload zone: large dashed border box, #0052FF dashed 2px, accent-subtle bg
    Upload icon in gradient circle (56px)
    "Drop answer sheets here" DM Sans 16px slate-600
    "JPG, PNG, PDF supported" DM Sans 13px slate-400
  After upload: card grid per answer sheet
    Status badges: PENDING(gray) / PROCESSING(blue, pulse) / DONE(green) / FAILED(red)
    Thin gradient progress bar below processing cards
  Results table:
    Student name, score number, percentage bar (gradient fill, slate-200 track), feedback icon
    Highlight rows: score < 50% gets red-50 bg tint

Analytics screen:
  2-column chart grid
  Left large card: score distribution bar chart, bars in #0052FF
  Right: pass rate donut chart, #0052FF filled arc, slate-200 track
  Bottom: per-question error rate table
    Rows where error rate > 50%: red-50 bg, red-600 text on that cell

---

COMPONENT RULES

Buttons:
  Primary: gradient bg, white text, rounded-xl, h-10, shadow-sm
    Hover: translateY(-1px), brightness(1.05), shadow-accent
  Secondary: white bg, slate border, slate-700 text, rounded-xl
    Hover: border-accent/30, shadow-sm
  Danger: red-50 bg, red-600 text, red-200 border
  Icon button: 36x36px, rounded-lg, slate-100 bg on hover

Cards:
  Default: white, rounded-xl, 1px #E2E8F0 border, shadow-sm
  Hover: shadow-md, translateY(-2px), transition 200ms
  Featured: 2px gradient border (wrapper div technique):
    <div style="background: gradient; padding: 2px; border-radius: 14px">
      <div style="background: white; border-radius: 12px">content</div>
    </div>

Inputs:
  Height: 40px, border 1px #E2E8F0, rounded-lg
  Focus: ring-2 ring-[#0052FF]/20, border-[#0052FF]
  Placeholder: slate-400
  Label: DM Sans 13px, slate-700, font-medium

Status badges:
  Rounded-full, px-2.5 py-0.5, text-xs, font-medium DM Sans
  Blue:  bg-blue-50   text-blue-600
  Green: bg-emerald-50 text-emerald-600
  Amber: bg-amber-50  text-amber-700
  Red:   bg-red-50    text-red-600
  AI:    gradient bg, white text, sparkle icon

Tables:
  Header: slate-50 bg, JetBrains Mono 11px UPPERCASE, slate-500
  Row: border-b slate-100, hover bg-slate-50/60
  Cell: py-3 px-4, DM Sans 14px

App motion (minimal — productivity context):
  All interactive elements: transition-all duration-150
  Card hover: translateY(-2px), shadow change, duration-200
  Sidebar active state: smooth bg transition, duration-150
  NO floating animations, NO entrance animations, NO pulsing inside app
  Exception only: OCR processing badge pulses opacity 1→0.5→1, 1.5s

---

AVOID:
  Purple gradients anywhere
  Serif fonts inside the app dashboard
  Heavy or distracting animations in the working app
  More than 3 colors in a single component
  Gradient text inside tables or data-dense views
  Cramped layouts — minimum py-4 inside cards
  Generic placeholder patterns with no visual hierarchy
```

---

## CSS VARIABLES (paste into global.css)

```css
:root {
  --accent:           #0052FF;
  --accent-light:     #4D7CFF;
  --accent-subtle:    rgba(0, 82, 255, 0.08);
  --gradient:         linear-gradient(135deg, #0052FF, #4D7CFF);

  --bg:               #F8FAFC;
  --surface:          #FFFFFF;
  --surface-muted:    #F1F5F9;
  --text-primary:     #0F172A;
  --text-secondary:   #64748B;
  --border:           #E2E8F0;

  --dark-bg:          #0A0F1E;
  --dark-surface:     #111827;
  --dark-surface-2:   #1A2235;
  --dark-text:        #F8FAFC;
  --dark-text-muted:  rgba(248, 250, 252, 0.6);
  --dark-border:      rgba(255, 255, 255, 0.08);

  --success:          #10B981;
  --warning:          #F59E0B;
  --danger:           #EF4444;

  --shadow-sm:        0 1px 3px rgba(0,0,0,0.06);
  --shadow-md:        0 4px 6px rgba(0,0,0,0.07);
  --shadow-lg:        0 10px 15px rgba(0,0,0,0.08);
  --shadow-accent:    0 4px 14px rgba(0, 82, 255, 0.25);
  --shadow-accent-lg: 0 8px 24px rgba(0, 82, 255, 0.35);

  --font-display:     'Plus Jakarta Sans', sans-serif;
  --font-body:        'DM Sans', sans-serif;
  --font-mono:        'JetBrains Mono', monospace;

  --radius-sm:        6px;
  --radius-md:        10px;
  --radius-lg:        14px;
  --radius-xl:        18px;
  --sidebar-width:    240px;
  --navbar-height:    56px;
}
```

---

## SCREEN CHECKLIST

**Landing:**
- [ ] Hero (dark, float mockup, gradient headline)
- [ ] Stats band (4 metrics)
- [ ] Feature rows (4 features, alternating)
- [ ] Value comparison table
- [ ] CTA section
- [ ] Footer

**App:**
- [ ] Login / Register
- [ ] Dashboard home
- [ ] Question Bank
- [ ] AI Question Generator
- [ ] Exam Builder
- [ ] OCR Upload + processing
- [ ] Grading results table
- [ ] Analytics charts