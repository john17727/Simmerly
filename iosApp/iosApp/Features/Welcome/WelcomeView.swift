import SwiftUI
import Shared

struct WelcomeView: View {
    let state: WelcomeState
    @Binding var serverAddress: String
    @Binding var username: String
    @Binding var password: String
    let onCredentialTypeChanged: (CredentialType) -> Void
    let onLogin: () -> Void

    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @FocusState private var focusedField: Field?
    @State private var passwordVisible = false

    private enum Field: Hashable {
        case serverAddress, username, password
    }

    var body: some View {
        Group {
            if horizontalSizeClass == .regular {
                expandedLayout
            } else {
                compactLayout
            }
        }
        .background(SimmerlyColor.background)
    }

    private var compactLayout: some View {
        VStack(spacing: 0) {
            WelcomeCollage()
                .frame(height: 220)
                .padding(.vertical, 16)
            header
            Spacer()
            loginForm
                .padding(16)
            Spacer()
        }
    }

    private var expandedLayout: some View {
        HStack(spacing: 0) {
            VStack {
                Spacer()
                logo
                Spacer()
            }
            .frame(maxWidth: .infinity)

            VStack {
                Spacer()
                loginForm
                    .frame(maxWidth: 500)
                Spacer()
            }
            .frame(maxWidth: .infinity)
            .background(SimmerlyColor.surface, in: RoundedRectangle(cornerRadius: 16))
            .padding(16)
        }
    }

    private var logo: some View {
        VStack(spacing: 16) {
            Image("LaunchLogo")
                .renderingMode(.template)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 120, height: 120)
                .foregroundStyle(.white)
            Text("Welcome to Simmerly")
                .font(SimmerlyFont.headlineLarge)
                .foregroundStyle(.white)
            Text("Your self-hosted recipe nook.")
                .font(SimmerlyFont.bodyLarge)
                .foregroundStyle(SimmerlyColor.tertiary)
        }
        .multilineTextAlignment(.center)
    }

    private var header: some View {
        VStack(spacing: 16) {
            Text("Welcome to Simmerly")
                .font(SimmerlyFont.headlineLarge)
                .foregroundStyle(SimmerlyColor.onSurface)
            Text("Your self-hosted recipe nook.")
                .font(SimmerlyFont.bodyLarge)
                .foregroundStyle(SimmerlyColor.tertiary)
        }
        .multilineTextAlignment(.center)
        .padding(.horizontal, 16)
    }

    private var loginForm: some View {
        VStack(spacing: 24) {
            TextField("Server Address", text: $serverAddress)
                .textFieldStyle(.roundedBorder)
                .textContentType(.URL)
                .keyboardType(.URL)
                .autocorrectionDisabled()
                .textInputAutocapitalization(.never)
                .focused($focusedField, equals: .serverAddress)
                .submitLabel(.next)
                .disabled(state.isLoading)

            Picker(
                "Credential Type",
                selection: Binding(get: { state.credentialType }, set: onCredentialTypeChanged)
            ) {
                Text("Credentials").tag(CredentialType.credentials)
                Text("API Token").tag(CredentialType.apiToken)
            }
            .pickerStyle(.segmented)
            .disabled(state.isLoading)

            if state.credentialType == .credentials {
                TextField("Username/Email", text: $username)
                    .textFieldStyle(.roundedBorder)
                    .textContentType(.username)
                    .autocorrectionDisabled()
                    .textInputAutocapitalization(.never)
                    .focused($focusedField, equals: .username)
                    .submitLabel(.next)
                    .disabled(state.isLoading)
            }

            passwordField

            Button(action: onLogin) {
                if state.isLoading {
                    ProgressView()
                        .tint(.white)
                        .frame(maxWidth: .infinity)
                } else {
                    Text("Login")
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .disabled(!state.isLoginButtonEnabled)
        }
    }

    private var passwordField: some View {
        Group {
            if passwordVisible {
                TextField(state.credentialType == .credentials ? "Password" : "Token", text: $password)
            } else {
                SecureField(state.credentialType == .credentials ? "Password" : "Token", text: $password)
            }
        }
        .textFieldStyle(.roundedBorder)
        .focused($focusedField, equals: .password)
        .submitLabel(.go)
        .onSubmit(onLogin)
        .disabled(state.isLoading)
        .overlay(alignment: .trailing) {
            Button {
                passwordVisible.toggle()
            } label: {
                Image(systemName: passwordVisible ? "eye.slash" : "eye")
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
            }
            .padding(.trailing, 8)
        }
    }
}
