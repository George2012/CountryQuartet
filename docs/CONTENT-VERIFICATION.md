# Phase 8 — Content Verification Report

Review of all 52 countries and 13 quartets in `app/src/main/assets/`.

**Nothing in the dataset was changed.** Every item below marked "decision needed"
or "suggested correction" is waiting for approval, as Phase 8 requires.

## How this was checked, and what that is worth

Each field was reviewed against the model's own knowledge (training cutoff
May 2026). **No external sources were consulted for this pass.** That is
adequate for stable facts such as currencies and long-established capitals, and
it is *not* adequate for anything that changes or is disputed — which is exactly
why those items appear under "decision needed" rather than being quietly
corrected or quietly left.

Three items below turn on facts that may have moved since the cutoff or that
reasonable sources disagree about. Those should be checked against a live source
before release. They are marked **[verify externally]**.

## 1. Verified entries

| What | Count | Result |
|---|---|---|
| Country names | 52 | Standard English short forms. No duplicates. |
| Country ids (ISO 3166-1 alpha-2) | 52 | All correct for the country named. |
| `flagAsset` paths | 52 | All `flags/<id>.png`, consistent with the id (enforced by a test). See §5. |
| Quartet membership | 52 | Every country in exactly one quartet; every quartet exactly four. Enforced by tests. |
| Capitals | 48 of 52 | Correct as written. The other four are in §2. |
| Currencies | 52 | All correct. One usage note in §3. |
| Primary language | 45 of 52 | Correct as the most widely used official language. See §2 and §3. |
| Fun facts | 50 of 52 | Accurate as written. Two need rewording — see §2. |

## 2. Questionable entries — decision needed

### Q1. Israel — capital "Jerusalem" **[verify externally]**
Israel designates Jerusalem as its capital; most states do not recognise this
and maintain embassies elsewhere. Reference works commonly list Jerusalem with a
note. This is a political question, not a factual one, and it is the single most
sensitive entry in a children's product.

Options: keep "Jerusalem"; use "Jerusalem (proclaimed)"; use "Tel Aviv" (factually
wrong as a capital, but where most embassies sit); or drop Israel from the deck.
**Recommendation: keep "Jerusalem", and decide deliberately rather than by default.**

### Q2. Sri Lanka — capital "Sri Jayawardenepura Kotte"
Factually correct: it is the legislative capital. Colombo is the commercial
capital and the executive/judicial seat, and is what most children's atlases show.
At 25 characters it is also the longest string in the dataset and the only one
that needed a two-line layout fix in Phase 6.

Options: keep as is; use "Colombo"; or use "Sri Jayawardenepura Kotte (Colombo)".
**Recommendation: keep the accurate name — the game is educational — and let the
fun fact carry Colombo if you want it mentioned.**

### Q3. Indonesia — capital "Jakarta" **[verify externally]**
Indonesia legislated in 2022 to move its capital to Nusantara in East Kalimantan,
with a phased transfer. Whether the formal designation has taken effect is
exactly the kind of fact that may have changed since the training cutoff.
**This must be checked against a current source before release.**

### Q4. Canada — language "English and French"
The dataset's convention (documented in the Phase 2 report) is *one* primary
language per country, with multilingual countries covered in the fun fact.
Canada is the only entry that breaks it. Switzerland, by contrast, holds
"German" with its four official languages in the fun fact.

Options: change Canada to "English" and move French into the fun fact
(consistent); or keep the exception (accurate, and Canadian bilingualism is
federal in a way Switzerland's is not).
**Recommendation: keep the exception and document it, rather than lose a true fact for tidiness.**

### Q5. India — fun fact "India's constitution recognises 22 official languages"
Imprecise. The Eighth Schedule lists 22 **scheduled** languages; the *official*
languages of the Union are Hindi and English. As written it conflates the two.

Suggested: "India's constitution recognises 22 scheduled languages."

### Q6. Mexico — fun fact "Chocolate was first made from cacao beans in ancient Mexico"
Overstated. The earliest known *use* of cacao is South American (Ecuador,
roughly 5,000 years ago). Mesoamerica is where cacao was developed into the
chocolate drink that reached the wider world.

Suggested: "Chocolate drinks were first made from cacao beans in ancient Mexico."

### Q7. Diacritics dropped throughout
ASCII spellings were used deliberately in Phase 2 to avoid encoding problems.
The pipeline has since proven to handle UTF-8 correctly, so the tradeoff can be
revisited. Affected entries:

| Field | Current | Correct form |
|---|---|---|
| Brazil capital | Brasilia | Brasília |
| Poland currency | Polish zloty | Polish złoty |
| Mongolia currency | Mongolian tugrik | Mongolian tögrög (both are used in English) |
| Vietnam currency | Vietnamese dong | Vietnamese đồng |
| Austria fun fact | Schoenbrunn | Schönbrunn |
| Vietnam fun fact | Ha Long Bay | Hạ Long Bay |

**Recommendation: restore diacritics for Brasília and Schönbrunn (plain
misspellings in English usage), and keep the anglicised currency names, which are
standard in English. Your call.**

## 3. Accurate, but worth knowing

These are correct as written. They are recorded so that a future reviewer does
not "fix" them into something wrong.

**Capitals**
- **Switzerland — Bern.** Switzerland has no constitutional capital; Bern is the
  federal city and is universally listed as the capital.
- **Tanzania — Dodoma.** Official capital since 1974 and seat of parliament.
  Dar es Salaam is larger and still hosts some government functions.

**Languages** — the field holds the single most widely used official language.
Countries where that hides something:
- **New Zealand — "English".** New Zealand's *official* languages are te reo
  Māori and New Zealand Sign Language; English is de facto official, never
  formally declared. The entry is right for a geography game but the reverse of
  what one might assume.
- **Japan, United States, Australia, Mexico — de facto.** None has a statutory
  national official language in the usual sense.
- **Finland** also has Swedish as an official language.
- **Morocco and Algeria** also have Tamazight (Amazigh) as an official language.
- **Kenya** (English), **Sri Lanka** (Tamil), **Israel** (Arabic has special
  status), **Pakistan** (English), **India** (English), **Fiji** (English, Fiji
  Hindi) and **Papua New Guinea** (English, Hiri Motu) all have co-official
  languages beyond the one listed.
- **Uganda — "English".** English is official; Luganda is the most widely spoken
  language but is not official, so "English" is the right choice for this field.

**Currencies**
- **China — "Chinese yuan".** The currency is the renminbi; the yuan is its
  unit. "Chinese yuan" is the ordinary English usage and is what a child will
  meet elsewhere. Keep.
- **Cuba — "Cuban peso".** Correct: the dual-currency system ended in 2021.
- **Peru — "Peruvian sol".** Correct: the nuevo sol was renamed sol in 2015.

**Fun facts that state a superlative**
These are all commonly cited and defensible, but they are the claims most likely
to draw a "well, actually":
- **Sweden — most islands of any country.** Widely cited; some counts rank
  Norway, Finland or Canada differently depending on what counts as an island.
- **Canada — more lakes than all other countries combined.** Commonly cited;
  Canada holds roughly 62% of the world's lakes.
- **Mongolia — coldest capital city.** By mean annual temperature. Correct.
- **Czechia — largest ancient castle complex.** Recognised by Guinness.
- **Thailand — longest place name.** Recognised by Guinness.

## 4. Geographic grouping concerns

Twelve of the thirteen quartets are uncontroversial. One is worth your decision.

### G1. Eastern Europe: Poland, Ukraine, Romania, Hungary
Poland and Hungary are frequently classified as **Central** Europe (they are, with
Czechia and Slovakia, the Visegrád Group), and both countries generally prefer
that description. The current split puts Czechia in Central Europe and Poland and
Hungary in Eastern Europe, which is defensible on a Cold-War-era reading but is
not how those countries describe themselves today.

This cannot be fixed by moving one country: Central Europe already holds four.
Any change reshuffles two quartets, so it is a content decision, not an edit.

Options:
1. **Keep as is.** Common in school atlases; requires no change. *Recommended
   unless you feel strongly — the grouping is a game mechanic, not a claim.*
2. Rename "Eastern Europe" to something less contested, e.g. "Central and Eastern
   Europe", leaving membership alone.
3. Reshuffle: e.g. Central Europe = Germany, Austria, Switzerland, Poland;
   Eastern Europe = Ukraine, Romania, Hungary, Czechia — which is worse, since
   Czechia is the least Eastern of the four.

### G2. North America includes Cuba
Correct on the continental definition, which places the Caribbean in North
America. Some readers expect North America to mean the USA, Canada and Mexico
only. No change recommended; recorded so it is a choice rather than an oversight.

### G3. Nordic Countries omits Iceland
Unavoidable — a quartet holds four and the Nordic region has five. Sweden,
Norway, Denmark and Finland are the natural four. No concern.

## 5. Technical / data concern

### T1. `flagAsset` points at files that do not exist
Every country carries `flagAsset: "flags/<id>.png"`, but there is no `flags/`
directory. Since Phase 6 the flag is rendered from the ISO country id as a pair
of regional indicator symbols (a flag emoji), so the field is unused.

This is not wrong data, but it is misleading: a future contributor will read it
as a promise that the assets exist. Options:
1. Keep the field as the documented slot for bundled flag images later.
   *Recommended, provided this report is the record of why it is empty.*
2. Remove `flagAsset` from the model and the JSON.
3. Add 52 flag images and use them — the only option that guarantees identical
   flags on every device, at the cost of APK size and image licensing.

## 6. Summary of suggested corrections

Nothing here has been applied. Ordered by how strongly it is recommended.

| # | Entry | Change | Why |
|---|---|---|---|
| Q5 | India fun fact | "22 official languages" → "22 scheduled languages" | Currently conflates scheduled with official |
| Q6 | Mexico fun fact | "Chocolate was first made…" → "Chocolate drinks were first made…" | Earliest cacao use is South American |
| Q7 | Brazil capital | Brasilia → Brasília | Misspelling in English usage |
| Q7 | Austria fun fact | Schoenbrunn → Schönbrunn | Misspelling in English usage |
| Q3 | Indonesia capital | Verify against a current source | May have changed since the training cutoff |
| Q1 | Israel capital | Confirm the choice deliberately | Politically sensitive; no purely factual answer |
| Q2 | Sri Lanka capital | Confirm the choice | Accurate but unfamiliar and very long |
| Q4 | Canada language | Confirm the exception | Only entry breaking the one-language convention |
| G1 | Eastern Europe quartet | Confirm or rename | Poland and Hungary are usually Central Europe |
| T1 | `flagAsset` | Keep, remove, or populate | Points at files that do not exist |

## 7. What is not covered

- **No external verification was performed.** The three **[verify externally]**
  items above need a live source before release.
- Fun facts were checked for accuracy, not for reading age. A separate pass with
  a 7-year-old reader in mind would be worthwhile.
- The dataset is English only. Nothing here has been reviewed for translation.
