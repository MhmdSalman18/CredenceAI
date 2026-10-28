class AppSettings {
  const AppSettings({
    this.userName = 'New User',
    this.darkMode = false,
    this.tracking = true,
    this.hideBalance = false,
    this.appLock = false,
    this.currency = 'INR',
  });
  final String userName;
  final bool darkMode;
  final bool tracking;
  final bool hideBalance;
  final bool appLock;
  final String currency;

  AppSettings copyWith({
    String? userName,
    bool? darkMode,
    bool? tracking,
    bool? hideBalance,
    bool? appLock,
    String? currency,
  }) => AppSettings(
    userName: userName ?? this.userName,
    darkMode: darkMode ?? this.darkMode,
    tracking: tracking ?? this.tracking,
    hideBalance: hideBalance ?? this.hideBalance,
    appLock: appLock ?? this.appLock,
    currency: currency ?? this.currency,
  );
}
