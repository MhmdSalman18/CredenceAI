class AppException implements Exception {
  const AppException(this.message);
  final String message;
  @override
  String toString() => message;
}

String friendlyError(Object error) => error is AppException
    ? error.message
    : 'Something went wrong. Please try again.';
