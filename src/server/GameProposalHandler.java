package server;

import com.google.gson.Gson;
import common.Message;
import common.ProposalMessage;
import common.ResponseMessage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

public class GameProposalHandler {
    private PlayerStatus playerStatus;
    private InetAddress clientAddr;
    private int clientUdpPort;

    public GameProposalHandler(PlayerStatus playerStatus, InetAddress clientAddr, int clientUdpPort) {
        this.playerStatus = playerStatus;
        this.clientAddr = clientAddr;
        this.clientUdpPort = clientUdpPort;
    }

    public void handleProposalRequest(String jsonMessage, ClientHandler client,DataOutputStream out) throws IOException {
        // 401: Tentativo di gioco senza sessione attiva
        if (this.playerStatus == null || !this.playerStatus.isOnline()) {
            ResponseMessage response = new ResponseMessage("submitProposalResponse", "401", "Accesso non autorizzato: effettua prima il login");
            new SendTCPResponse(response, out);
            return;
        }

        // 403: Partita già terminata
        if (this.playerStatus.isGameEnded()) {
            ResponseMessage response = new ResponseMessage("submitProposalResponse", "403", "Partita già conclusa");
            new SendTCPResponse(response, out);
            return;
        }

        Gson gson = new Gson();
        ProposalMessage proposalMessage = gson.fromJson(jsonMessage, ProposalMessage.class);
        ArrayList<String> proposal = proposalMessage.getProposal();

        String result = this.playerStatus.removeWordsGuessed(proposal);

        if (result == null) {
            // 422: Proposta formata regolarmente ma errata (-1 vita, -4 punti)
            ResponseMessage response = new ResponseMessage(
                    "submitProposalResponse",
                    "422",
                    "Proposta errata: combinazione non valida. Vite rimaste: " + (4 - this.playerStatus.getMistakes())
            );
            new SendTCPResponse(response, out);
        } else if (result.equals("MALFORMED")) {
            // 400: Proposta malformata (non sono 4 parole o parole non presenti)
            ResponseMessage response = new ResponseMessage(
                    "submitProposalResponse",
                    "400",
                    "Proposta malformata: inviare 4 parole valide ancora in gioco (nessuna penalità applicata)"
            );
            new SendTCPResponse(response, out);
        } else if (result.equals("ENDGAME")) {
            // 403 / Fine vite: notifica esaurimento e fine partita
            ResponseMessage response = new ResponseMessage(
                    "submitProposalResponse",
                    "403",
                    "Vite esaurite: partita terminata"
            );
            new SendTCPResponse(response, out);
            client.MatchEnd();
        } else if (result.equals("WIN")) {
            // 200: Vittoria partita
            ResponseMessage response = new ResponseMessage(
                    "submitProposalResponse",
                    "200",
                    "Complimenti! Hai completato tutti i gruppi e vinto la partita!");
            new SendTCPResponse(response, out);
            client.MatchEnd();
        } else {
            // 200: Gruppo indovinato
            ResponseMessage response = new ResponseMessage("submitProposalResponse", "200", "Gruppo corretto: " + result);
            new SendTCPResponse(response, out);
        }
    }
}