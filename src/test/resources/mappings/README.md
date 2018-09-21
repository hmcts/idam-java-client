# Mappings for wiremock

These wiremock mappings were generated using wiremock recorder standalone:
http://wiremock.org/docs/record-playback/

1. Start up wiremock standalone and tell it to record the idam url
2. Change the tests idam url to point at the wiremock standalone
3. Run the test
4. Stop recording
5. Copy in the generated mappings and tweak as necessary