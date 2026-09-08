# B&E Messenger — v0.5.0 UI POLISHED

Demo Android client for GitHub → Codemagic.

## v0.5
- rebuilt login button with explicit foreground/background rendering;
- safe system-window layout to prevent clipped top content;
- larger, readable message bubbles with high-quality wrapping;
- visible author labels;
- private 1:1 conversation presentation;
- working + and ⋮ actions;
- emoji picker;
- image/file picker;
- image preview inside an attachment bubble;
- demo search, conversation info, clear, mute and block actions;
- E2E status is presented as a design/architecture indicator only — no real network/E2E is claimed in this demo.

## Build
Use Codemagic workflow `android-debug`.
Artifact: `app/build/outputs/apk/debug/app-debug.apk`

This version still has no backend connection.
Next stage: OVH backend + real accounts + private rooms + real E2EE.
