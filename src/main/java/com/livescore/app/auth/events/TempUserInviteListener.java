package com.livescore.app.auth.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

import com.livescore.app.auth.utils.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TempUserInviteListener {

    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTempUserCreated(TempUserCreatedEvent event) {
        String link = frontendUrl + "/auth/accept-invite?token=" + event.token();
        String name = HtmlUtils.htmlEscape(event.firstName());
        String roleTitle = formatRoleTitle(event.role().name());
        String subject = "You're invited to Livescore — " + roleTitle + " access";

        String html = buildInviteEmail(name, roleTitle, link);

        try {
            emailService.sendHtml(event.email(), subject, html);
        } catch (Exception e) {
            log.error("Failed to send invite email to {}", event.email(), e);
        }
    }

    /* ------------------------------------------------------------------ */
    /*  PREMIUM EMAIL TEMPLATE                                             */
    /* ------------------------------------------------------------------ */

    private String buildInviteEmail(String name, String roleTitle, String link) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta http-equiv="X-UA-Compatible" content="IE=edge">
                <meta name="color-scheme" content="dark light">
                <meta name="supported-color-schemes" content="dark light">
                <title>Livescore Invitation</title>
            </head>
            <body style="margin:0;padding:0;background-color:#05070c;font-family:'Inter',-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;-webkit-font-smoothing:antialiased;-moz-osx-font-smoothing:grayscale;">

                <!-- ===== PREHEADER (hidden, shows in inbox preview) ===== -->
                <div style="display:none;font-size:1px;color:#05070c;line-height:1px;max-height:0;max-width:0;opacity:0;overflow:hidden;mso-hide:all;">
                    You've been granted %s access to Livescore. Set your password to activate your account.
                    &#847;&zwnj;&nbsp;&#847;&zwnj;&nbsp;&#847;&zwnj;&nbsp;&#847;&zwnj;&nbsp;&#847;&zwnj;&nbsp;
                </div>

                <!-- ===== WRAPPER ===== -->
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0"
                       style="background-color:#05070c;background-image:radial-gradient(circle at 15%% 0%%,rgba(59,130,246,0.14) 0%%,transparent 45%%),radial-gradient(circle at 85%% 100%%,rgba(139,92,246,0.12) 0%%,transparent 45%%);padding:64px 24px;">
                    <tr>
                        <td align="center">

                            <!-- ===== TOP ACCENT BAR ===== -->
                            <table role="presentation" width="600" cellpadding="0" cellspacing="0" border="0"
                                   style="max-width:600px;width:100%%;">
                                <tr>
                                    <td style="height:3px;background-image:linear-gradient(90deg,#3b82f6 0%%,#8b5cf6 50%%,#ec4899 100%%);border-radius:24px 24px 0 0;"></td>
                                </tr>
                            </table>

                            <!-- ===== CARD ===== -->
                            <table role="presentation" width="600" cellpadding="0" cellspacing="0" border="0"
                                   style="max-width:600px;width:100%%;background-color:#0d1420;background-image:linear-gradient(170deg,#101827 0%%,#0a0f18 100%%);border-radius:0 0 24px 24px;overflow:hidden;box-shadow:0 0 0 1px rgba(255,255,255,0.05),0 40px 80px -20px rgba(0,0,0,0.95),0 20px 40px -20px rgba(0,0,0,0.8);">

                                <!-- ===== HEADER / LOGO ===== -->
                                <tr>
                                    <td align="center"
                                        style="background-image:linear-gradient(135deg,#0a0f1a 0%%,#141e30 50%%,#0a0f1a 100%%);padding:44px 24px 40px;border-bottom:1px solid rgba(255,255,255,0.06);">

                                        <table role="presentation" cellpadding="0" cellspacing="0" border="0">
                                            <tr>
                                                <td valign="middle"
                                                    style="background-image:linear-gradient(135deg,#3b82f6 0%%,#8b5cf6 100%%);width:44px;height:44px;border-radius:13px;text-align:center;vertical-align:middle;font-size:22px;font-weight:800;color:#ffffff;box-shadow:0 8px 20px rgba(59,130,246,0.5),inset 0 1px 0 rgba(255,255,255,0.25);">
                                                    &#9889;
                                                </td>
                                                <td width="12"></td>
                                                <td valign="middle"
                                                    style="font-size:28px;font-weight:800;letter-spacing:-0.8px;color:#ffffff;">
                                                    Livescore
                                                </td>
                                            </tr>
                                        </table>

                                        <!-- Tagline -->
                                        <p style="margin:18px 0 0;font-size:11px;font-weight:600;color:#64748b;letter-spacing:3px;text-transform:uppercase;">
                                            Live Sports Intelligence
                                        </p>

                                    </td>
                                </tr>

                                <!-- ===== BODY ===== -->
                                <tr>
                                    <td style="padding:52px 48px 44px;color:#cbd5e1;font-size:16px;line-height:1.7;">

                                        <!-- Eyebrow -->
                                        <p style="margin:0 0 14px;font-size:11px;font-weight:700;color:#60a5fa;letter-spacing:2.5px;text-transform:uppercase;">
                                            &#9679;&nbsp; Exclusive Invitation
                                        </p>

                                        <!-- Greeting -->
                                        <p style="margin:0 0 24px;font-size:24px;font-weight:700;color:#ffffff;letter-spacing:-0.5px;line-height:1.35;">
                                            Hi <span style="color:#60a5fa;">%s</span>,
                                        </p>

                                        <!-- Intro -->
                                        <p style="margin:0 0 22px;color:#94a3b8;font-size:15.5px;line-height:1.75;">
                                            You've been personally invited to join
                                            <span style="color:#ffffff;font-weight:700;">Livescore</span>.
                                            Your access has been provisioned with the following role:
                                        </p>

                                        <!-- Role card -->
                                        <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%"
                                               style="margin:0 0 26px;">
                                            <tr>
                                                <td style="background-image:linear-gradient(135deg,rgba(59,130,246,0.10) 0%%,rgba(139,92,246,0.10) 100%%);border:1px solid rgba(99,145,255,0.28);border-radius:14px;padding:18px 24px;box-shadow:0 8px 24px -8px rgba(59,130,246,0.35),inset 0 1px 0 rgba(255,255,255,0.04);">
                                                    <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%">
                                                        <tr>
                                                            <td valign="middle" width="44"
                                                                style="width:44px;height:44px;background-image:linear-gradient(135deg,#3b82f6 0%%,#8b5cf6 100%%);border-radius:11px;text-align:center;vertical-align:middle;font-size:18px;color:#ffffff;box-shadow:0 4px 12px rgba(59,130,246,0.4);">
                                                                &#9733;
                                                            </td>
                                                            <td width="16"></td>
                                                            <td valign="middle">
                                                                <p style="margin:0;font-size:10.5px;font-weight:700;color:#64748b;letter-spacing:2px;text-transform:uppercase;">
                                                                    Your Role
                                                                </p>
                                                                <p style="margin:4px 0 0;font-size:17px;font-weight:700;color:#ffffff;letter-spacing:-0.2px;">
                                                                    %s
                                                                </p>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>

                                        <!-- Instruction -->
                                        <p style="margin:0 0 8px;color:#94a3b8;font-size:15.5px;line-height:1.75;">
                                            Click the button below to secure your account and create your password. Access is granted immediately after setup.
                                        </p>

                                        <!-- CTA Button -->
                                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                                            <tr>
                                                <td align="center" style="padding:40px 0 34px;">
                                                    <a href="%s"
                                                       target="_blank"
                                                       rel="noopener noreferrer"
                                                       style="display:inline-block;background-image:linear-gradient(135deg,#3b82f6 0%%,#2563eb 60%%,#1d4ed8 100%%);color:#ffffff;text-decoration:none;padding:18px 56px;font-size:16px;font-weight:700;border-radius:100px;letter-spacing:0.4px;box-shadow:0 18px 36px -10px rgba(37,99,235,0.75),0 6px 14px -4px rgba(37,99,235,0.5),inset 0 1px 0 rgba(255,255,255,0.25),inset 0 -1px 0 rgba(0,0,0,0.2);">
                                                        Activate My Account &nbsp;&#8594;
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>

                                        <!-- Divider -->
                                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                                            <tr>
                                                <td style="height:1px;background-image:linear-gradient(90deg,transparent,rgba(255,255,255,0.10),transparent);"></td>
                                            </tr>
                                        </table>

                                        <!-- Security note -->
                                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0"
                                               style="margin:28px 0 0;">
                                            <tr>
                                                <td valign="top" width="20"
                                                    style="width:20px;padding-top:2px;font-size:14px;color:#60a5fa;">
                                                    &#128274;
                                                </td>
                                                <td valign="top"
                                                    style="font-size:13.5px;color:#7c8ba1;line-height:1.7;">
                                                    <span style="color:#cbd5e1;font-weight:600;">Link expires in 24 hours.</span>
                                                    If you weren't expecting this invitation, you can safely ignore this email — no account will be created.
                                                </td>
                                            </tr>
                                        </table>

                                    </td>
                                </tr>

                                <!-- ===== SIGNATURE ===== -->
                                <tr>
                                    <td align="center"
                                        style="padding:0 48px 36px;">
                                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                                            <tr>
                                                <td style="height:1px;background-image:linear-gradient(90deg,transparent,rgba(255,255,255,0.08),transparent);"></td>
                                            </tr>
                                        </table>
                                        <p style="margin:24px 0 0;font-size:13px;color:#64748b;line-height:1.6;font-style:italic;">
                                            — The Livescore Team
                                        </p>
                                    </td>
                                </tr>

                                <!-- ===== FOOTER ===== -->
                                <tr>
                                    <td align="center"
                                        style="background-color:#070b13;padding:30px 24px;border-top:1px solid rgba(255,255,255,0.05);">

                                        <p style="margin:0 0 14px;font-size:11px;font-weight:700;color:#475569;letter-spacing:2.5px;text-transform:uppercase;">
                                            Livescore
                                        </p>

                                        <p style="margin:0 0 12px;font-size:12.5px;color:#5a6b82;letter-spacing:0.3px;line-height:1.6;">
                                            &copy; 2026 Livescore. All rights reserved.
                                        </p>

                                        <p style="margin:14px 0 0;font-size:12.5px;">
                                            <a href="#" style="color:#7c8ba1;text-decoration:none;margin:0 10px;">Privacy</a>
                                            <span style="color:#1e293b;">&bull;</span>
                                            <a href="#" style="color:#7c8ba1;text-decoration:none;margin:0 10px;">Terms</a>
                                            <span style="color:#1e293b;">&bull;</span>
                                            <a href="#" style="color:#7c8ba1;text-decoration:none;margin:0 10px;">Support</a>
                                        </p>

                                    </td>
                                </tr>

                            </table>
                            <!-- ===== /CARD ===== -->

                            <!-- Bottom caption -->
                            <p style="margin:24px 0 0;font-size:11px;color:#334155;letter-spacing:1.5px;text-transform:uppercase;">
                                Sent with intent &middot; Powered by Livescore
                            </p>

                        </td>
                    </tr>
                </table>
                <!-- ===== /WRAPPER ===== -->

            </body>
            </html>
            """.formatted(roleTitle, name, roleTitle, link);
    }

    /* ------------------------------------------------------------------ */
    /*  HELPERS                                                            */
    /* ------------------------------------------------------------------ */

    private String formatRoleTitle(String roleName) {
        String[] parts = roleName.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                  .append(part.substring(1))
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}