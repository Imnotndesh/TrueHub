# TrueHub — Backlog / TODO

Work items for future dev sessions. Pick these up in order when picking a new task.

1. **App-wide cache improvement**
   Improve caching across the app (API responses, pooled data, images, instance data) to reduce network chatter and speed up screens.

2. **Easier account switching and preloading of other accounts**
   Make switching between saved TrueNAS accounts smoother, and preload/prime data for the other accounts in the background so switching feels instant.

3. **More widgets**
   Add additional home-screen widgets beyond the existing Quick Launch / Apps Updates / Storage Pools ones.

4. **Fix search navigation**
   Search currently doesn't return to the previous page (it goes to the homepage by default, or doesn't close properly). Fix back-navigation and dismissal of the search overlay.

5. **Dynamic performance polling based on the selected filter**
   The performance screen should adjust its polling interval depending on which metric/timescale filter is selected (e.g. poll faster for short windows, slower for long ones).

6. **Fingerprint auth in the Privacy section**
   Add biometric (fingerprint) authentication option to the Settings → Privacy section (gate sensitive actions/info behind it).

7. **Add profile info for the user**
   Show the currently logged-in user's profile information (name, account details) somewhere sensible (Profile screen).

8. **Fix the Licenses page**
   The Licenses page is broken/incomplete; fix data loading and rendering of the open-source licenses.

9. **Figure out shares mounting or access from the app**
   Explore supporting mounting or direct access to TrueNAS shares from within the app (SMB/NFS) or opening them externally.
