package hr.fer.progi.raketa.onlinegalerija.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.fer.progi.raketa.onlinegalerija.model.*;
import hr.fer.progi.raketa.onlinegalerija.model.Artwork;
import hr.fer.progi.raketa.onlinegalerija.model.Collection;
import hr.fer.progi.raketa.onlinegalerija.repository.ContestRepository;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class service {
    @Autowired
    private AdminController ac;

    public ResponseEntity<Map<String, Map<String, String>>> produceCollections(Set<Collection> collections) throws JsonProcessingException {
        Map<String, Map<String, String>> retMap = new HashMap<>();

        for(Collection c : collections){
            Map<String, String> collMap = new HashMap<>();
            for(Artwork a : c.getArtworks())
                collMap.put(artToMapJson(a), Base64.getEncoder().encodeToString(a.getImageInBytes()));

            retMap.put(collToMapJson(c), collMap);
        }

        return new ResponseEntity<>(retMap, HttpStatus.OK);
    }

    public ResponseEntity<Map<String, String>> produceCollectionsSingles(Set<Collection> collections) throws JsonProcessingException {
        Map<String, String> retMap = new HashMap<>();

        for(Collection c : collections){
            String image64 = "";
            if(c.getArtworks().iterator().hasNext()) {
                Artwork a = c.getArtworks().iterator().next();
                image64 = Base64.getEncoder().encodeToString(a.getImageInBytes());
            }

            retMap.put(collToMapJson(c), image64);
        }

        return new ResponseEntity<>(retMap, HttpStatus.OK);
    }

    public ResponseEntity<Map<String, String>> produceCollectionsList(Set<Collection> collections) throws JSONException, JsonProcessingException {
        Map<String, String> collectionList = new HashMap<>();

        for(Collection c : collections)
            collectionList.put(c.getName(), collToMapJson(c));

        return new ResponseEntity<>(collectionList, HttpStatus.OK);
    }

    public ResponseEntity<AbstractMap.SimpleEntry<String, Map<String, Map<String, String>>>> produceExhibition(Exhibition exhibition) throws JsonProcessingException {
        String exDesc = exToMapJson(exhibition);

        return new ResponseEntity<>(new AbstractMap.SimpleEntry<>(
                exDesc,
                produceCollections(exhibition.getCollections()).getBody()
        ), HttpStatus.OK);
    }

    public ResponseEntity<AbstractMap.SimpleEntry<String, Map<String,String>>> produceCollection(Collection collection)throws JsonProcessingException {
        String collDesc = collToMapJson(collection);
        Map<String, String> collMap = new HashMap<>();

        for(Artwork a : collection.getArtworks())
            collMap.put(artToMapJson(a), Base64.getEncoder().encodeToString(a.getImageInBytes()));

        return new ResponseEntity<>(new AbstractMap.SimpleEntry<>(
                collDesc,
                collMap
        ), HttpStatus.OK);
    }

    public ResponseEntity<Map<String, String>> produceExhibitionSingles(Set<Exhibition> exhibitions) throws JsonProcessingException {
        Map<String, String> retMap = new HashMap<>();

        for(Exhibition e : exhibitions){
            if(e.getBeginDateTime().plus(e.getDuration()).isBefore(LocalDateTime.now())) {
                ac.closeExInternal(e.getName());
                continue;
            }
            String image64 = "";
            if(e.getCollections().iterator().hasNext()) {
                if(e.getCollections().iterator().next().getArtworks().iterator().hasNext()) {
                    Artwork a = e.getCollections().iterator().next().getArtworks().iterator().next();
                    image64 = Base64.getEncoder().encodeToString(a.getImageInBytes());
                }
            }
            retMap.put(exToMapJson(e), image64);
        }

        return new ResponseEntity<>(retMap, HttpStatus.OK);
    }

    public ResponseEntity<Map<String, Set<String>>> produceApplications(Contest contest){
        Map<String, Set<String>> retMap = new HashMap<>();

        System.out.println(contest.getApplications().size());

        for(ContestApplication ca : contest.getApplications()){
            if(retMap.get(ca.getArtist().getEmail()) == null)
                retMap.put(ca.getArtist().getEmail(), ca.getCollections().stream().map(c -> c.getName()).collect(Collectors.toSet()));
            else
                for(Collection c : ca.getCollections())
                    retMap.get(ca.getArtist().getEmail()).add(c.getName());
        }

        return new ResponseEntity<>(retMap, HttpStatus.OK);
    }

    public ResponseEntity<Map<UUID, Map<String, String>>> produceComments(List<Comment> commentList) {
        Map<UUID, Map<String, String>> commentMap = new LinkedHashMap<>();

        for (Comment c : commentList) {
            commentMap.put(c.getCommentId(), produceCommentMapJson(c));
        }

        return new ResponseEntity<>(commentMap, HttpStatus.OK);
    }

    public ResponseEntity<List<Map<String, String>>> produceTransactions(List<Transaction> transactions) {
        List<Map<String, String>> transactionJsonList = new ArrayList<>();

        for (Transaction t : transactions)
            transactionJsonList.add(produceTransactionJson(t));

        System.out.println("Velicina liste je: " + transactionJsonList.size());

        return new ResponseEntity<>(transactionJsonList, HttpStatus.OK);
    }

    public ResponseEntity<Map<String, String>> produceContests(Set<Contest> contests) throws JsonProcessingException {
        Map<String, String> retMap = new HashMap<>();

        for(Contest c : contests)
            retMap.put(c.getWorkingName(), contestToMapJson(c));

        return new ResponseEntity<>(retMap, HttpStatus.OK);
    }

//    private String produceCollectionJson(Collection c) throws JSONException {
//        StringBuilder sb = new StringBuilder();
//        sb.append("{");
//        sb.append("\"name\": \"").append(c.getName()).append("\",");
//        sb.append("\"description\": \"").append(c.getDescription()).append("\",");
//        sb.append("\"style\": \"").append(c.getStyle().toString()).append("\"");
//        sb.append("}");
//
//
//        System.out.println(sb.toString());
//        return sb.toString();
//    }

    private String collToMapJson(Collection collection) throws JsonProcessingException {
        Map<String, String> res = new HashMap<>();
        res.put("Author", collection.getArtist().getName() + " " + collection.getArtist().getSurname());
        res.put("Name", collection.getName());
        res.put("Description", collection.getDescription());
        res.put("Style", collection.getStyle().toString());
        res.put("NumOfArtworks", String.valueOf(collection.getArtworks().size()));
        return new ObjectMapper().writeValueAsString(res);
    }

    private String artToMapJson(Artwork artwork) throws JsonProcessingException {
        Map<String, String> res = new HashMap<>();
        res.put("id", artwork.getId().toString());
        res.put("Name", artwork.getName());
        res.put("Description", artwork.getDescription());
        res.put("Style", artwork.getStyle().toString());
        res.put("fileType", artwork.getFileType());
        res.put("Price", String.valueOf(artwork.getPrice()));
        res.put("sold", String.valueOf(artwork.getTransaction() == null ? 0 : 1));
        return new ObjectMapper().writeValueAsString(res);
    }

    private String exToMapJson(Exhibition exhibition) throws JsonProcessingException {
        Map<String, String> res = new HashMap<>();
        res.put("Name", exhibition.getName());
        res.put("Description", exhibition.getDescription());
        res.put("Style", exhibition.getStyle().toString());
        res.put("BeginDate", exhibition.getBeginDateTime().toString());
        res.put("Duration", exhibition.getDuration().toString());
        res.put("Provision", String.valueOf(exhibition.getProvision()));
        res.put("Artists", artistsByComma(exhibition.getArtists()));
        return new ObjectMapper().writeValueAsString(res);
    }

    private String contestToMapJson(Contest contest) throws JsonProcessingException {
        Map<String, String> res = new HashMap<>();
        res.put("Name", contest.getWorkingName());
        res.put("Description", contest.getDescription());
        res.put("Style", contest.getStyle().toString());
        res.put("BeginDate", contest.getBeginDateTime().toString());
        res.put("Duration", contest.getDuration().toString());
        res.put("Provision", String.valueOf(contest.getProvision()));
        return new ObjectMapper().writeValueAsString(res);
    }

    private String artistsByComma(Set<Artist> artists){
        StringBuilder sb = new StringBuilder();
        for(Artist a : artists)
            sb.append(a.getName() + " " + a.getSurname()).append(',');
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private String removeSlashes(String text){
        char[] arr = text.toCharArray();
        StringBuilder sb = new StringBuilder();

        for(char c : arr)
            if(c != '\\')
                sb.append(c);

        return sb.toString();
    }

//    private String produceArtworkJson(Artwork a) throws JSONException {
//        StringBuilder sb = new StringBuilder();
//        sb.append("{");
//        sb.append("\"id\": \"").append(a.getId()).append("\",");
//        sb.append("\"name\": \"").append(a.getName()).append("\",");
//        sb.append("\"description\": \"").append(a.getDescription()).append("\",");
//        sb.append("\"style\": \"").append(a.getStyle().toString()).append("\",");
//        sb.append("\"price\": \"").append(a.getPrice()).append("\"");
//        sb.append("}");
//        System.out.println(sb.toString());
//        return sb.toString();
//    }

    private Map<String, String> produceCommentMapJson(Comment c) {
        Visitor commenter = c.getVisitor();
        int isCommentByArtist = 0;
        if(commenter.getId().equals(c.getArtwork().getCollection().getArtist().getId()))
            isCommentByArtist = 1;

        Map<String, String> retMap = new HashMap<>();

        retMap.put("id", c.getCommentId().toString());
        retMap.put("name", c.getVisitor().getName() + " " + c.getVisitor().getSurname());
        retMap.put("isByArtist", String.valueOf(isCommentByArtist));
        retMap.put("content", c.getContent());
        return retMap;
    }

    private Map<String, String> produceTransactionJson(Transaction t) {
        Map<String, String> retMap = new HashMap<>();

        retMap.put("artworkName", t.getArtwork().getName());
        retMap.put("payerName", t.getPayer().getName() + " " + t.getPayer().getSurname());
        retMap.put("artistName", t.getReceiver().getName() + " " + t.getReceiver().getSurname());
        retMap.put("provision", String.valueOf(t.getProvision()));
        retMap.put("price", String.valueOf(t.getArtwork().getPrice()));
        retMap.put("byteArray64", Base64.getEncoder().encodeToString(t.getArtwork().getImageInBytes()));

        return retMap;
    }


}
