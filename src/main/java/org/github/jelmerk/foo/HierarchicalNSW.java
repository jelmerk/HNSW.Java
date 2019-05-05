package org.github.jelmerk.foo;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class HierarchicalNSW<ID, ITEM extends Item<ID>> implements AlgorithmInterface<ID, ITEM> {

    private int m;
    private int maxM;
    private int maxM0;
    private int efConstruction;

    private double mult;
    private double revSize;
//    private int maxLevel;

//    VisitedListPool *visited_list_pool_;

//    private int enterpointNode;

    private Node enterpointNode;


    private DistanceFunction fstdistfunc;

    private final List<Item<ID>> items;
    private List<Node> nodes;
    private Map<ID, ITEM> idLookup;

    private Random levelGenerator;

    private int ef;
    private ReentrantLock global;

    public HierarchicalNSW(DistanceFunction fstdistfunc, int m, int efConstruction, int randomSeed) {

        this.fstdistfunc = fstdistfunc;

        this.m = m;
        this.maxM = m;
        this.maxM0 = m * 2;

        this.efConstruction = Math.max(efConstruction, m);

        this.ef = 10;

        this.levelGenerator = new Random(randomSeed);


        //initializations for special treatment of the first node
        this.enterpointNode = null;
//        this.maxLevel = -1;


        this.mult = 1 / Math.log(1d * m);
        this.revSize = 1d / this.mult;


        this.global = new ReentrantLock();

        this.items = Collections.synchronizedList(new ArrayList<>());
    }


    @Override
    public ITEM getById(ID id) {
        return idLookup.get(id);
    }

    @Override
    public void addPoint(ITEM item) {
        addPoint(item, -1);
    }


    private int addPoint(ITEM item, int level) {

        int curC;

        synchronized (items) {
            curC = items.size();
            items.add(item);
            idLookup.put(item.getId(), item); // expected unique, if not will overwrite
        }

        synchronized (items.get(curC)) {

            int curlevel = getRandomLevel(mult);

            if (level > 0) {
                curlevel = level;  // TODO HOW does this even work, wouldnt it always become -1
            }

            List<List<Integer>> connections = new ArrayList<>(curlevel + 1);
            for (int layer = 0; layer <= curlevel; layer++) {
                // M + 1 neighbours to not realloc in addConnection when the level is full
                int layerM = (layer == 0 ? 2 * m : m) + 1;
                connections.add(new ArrayList<>(layerM));
            }

            Node node = new Node(curC, connections);

            nodes.add(node);

            global.lock(); // TODO make sure we unlock this

            int maxLevelCopy = enterpointNode.maxLayer();

            if (curlevel <= maxLevelCopy) {
                global.unlock();
            }

            try {

                Node currObj = enterpointNode;








        /*
            memset(data_level0_memory_ + cur_c * size_data_per_element_ + offsetLevel0_, 0, size_data_per_element_);

            // Initialisation of the data and label
            memcpy(getExternalLabeLp(cur_c), &label, sizeof(labeltype));
            memcpy(getDataByInternalId(cur_c), data_point, data_size_);
        */


                // if (c) is the same as if (c != 0). And if (!c) is the same as if (c == 0).

                if (curlevel != 0) {
//            linkLists_[cur_c] = (char *) malloc(size_links_per_element_ * curlevel + 1);
//            memset(linkLists_[cur_c], 0, size_links_per_element_ * curlevel + 1);
                }

                if (currObj != null) {

                    if (curlevel < maxLevelCopy) {

                        // TODO JK , the original code passes in spaceInterface.get_dist_func_param as 3rd argument to the distance function
                        // TODO JK i guess we need to find the item for the id here or something in getDataByInternalId

                        float curDist = fstdistfunc.distance(item.getVector(), items.get(currObj.id).getVector());

                        for (int activeLevel = maxLevelCopy; activeLevel > curlevel; activeLevel--) {

                            boolean changed = true;

                            while (changed) {
                                changed = false;

                                synchronized (items.get(currObj)) { // TODO jk : ehm i guess just getting the item from an unsynchronized list is not thread safe.. though if its an array what could go wrong


                                    for (int i = 0; i < size; i++) {

                                        int cand = ???
                                        if (cand < 0 || cand > maxElements) {
                                            throw new IllegalStateException("cand error");
                                        }

                                        TDistance d = fstdistfunc.distance(dataPoint, items.get(cand)); // TODO

                                        if (d.compareTo(curDist) < 0) {
                                            curDist = d;
                                            currObj = cand;
                                            changed = true;
                                        }
                                    }


                                }

                            }
                        }

                        for (int level = Math.min(curlevel, maxLevelCopy); level >= 0; level--) {
                            if (level > maxLevelCopy || level < 0) {
                                throw new IllegalStateException("Level error");
                            }

                            PriorityQueue<Pair<TDistance, Integer>> topCandidates = searchBaseLayer(currObj, dataPoint, level);

                            mutuallyConnectNewElement(dataPoint, curC, topCandidates, level);
                        }

                    }

                } else {
                    // Do nothing for the first element
                    this.enterpointNode = 0;
                    this.maxLevel = curlevel;
                }

                //Releasing lock for the maximum level
                if (curlevel > maxLevelCopy) {
                    this.enterpointNode = curC;
                    this.maxLevel = curlevel;
                }
                return curC;


            } finally {
                if (global.isHeldByCurrentThread()) {
                    global.unlock();
                }
            }
        }









        /*

            if ((signed)currObj != -1) {


                if (curlevel < maxlevelcopy) {

                    dist_t curdist = fstdistfunc_(data_point, getDataByInternalId(currObj), dist_func_param_);
                    for (int level = maxlevelcopy; level > curlevel; level--) {


                        bool changed = true;
                        while (changed) {
                            changed = false;
                            int *data;
                            std::unique_lock <std::mutex> lock(link_list_locks_[currObj]);
                            data = (int *) (linkLists_[currObj] + (level - 1) * size_links_per_element_);
                            int size = *data;
                            tableint *datal = (tableint *) (data + 1);
                            for (int i = 0; i < size; i++) {
                                tableint cand = datal[i];
                                if (cand < 0 || cand > max_elements_)
                                    throw std::runtime_error("cand error");
                                dist_t d = fstdistfunc_(data_point, getDataByInternalId(cand), dist_func_param_);
                                if (d < curdist) {
                                    curdist = d;
                                    currObj = cand;
                                    changed = true;
                                }
                            }
                        }
                    }
                }

                for (int level = std::min(curlevel, maxlevelcopy); level >= 0; level--) {
                    if (level > maxlevelcopy || level < 0)
                        throw std::runtime_error("Level error");

                    std::priority_queue<std::pair<dist_t, tableint>, std::vector<std::pair<dist_t, tableint>>, CompareByFirst> top_candidates = searchBaseLayer(
                            currObj, data_point, level);
                    mutuallyConnectNewElement(data_point, cur_c, top_candidates, level);
                }


            } else {
                // Do nothing for the first element
                enterpoint_node_ = 0;
                maxlevel_ = curlevel;

            }

            //Releasing lock for the maximum level
            if (curlevel > maxlevelcopy) {
                enterpoint_node_ = cur_c;
                maxlevel_ = curlevel;
            }
            return cur_c;
         */

    }



    private void getNeighborsByHeuristic2(PriorityQueue<Pair<Float, Integer>> topCandidates, int m) {

        if (topCandidates.size() < m) {
            return;
        }

        PriorityQueue<Pair<Float, Integer>> queueClosest = new PriorityQueue<>();
        List<Pair<Float, Integer>> returnList = new ArrayList<>();

        while(!topCandidates.isEmpty()) {
            Pair<Float, Integer> element = topCandidates.remove();
            queueClosest.add(Pair.of(-element.getFirst(), element.getSecond()));
        }

        while(!queueClosest.isEmpty()) {
            if (returnList.size() >= m) {
                break;
            }

            Pair<Float, Integer> currentPair = queueClosest.remove();

            float distToQuery = -currentPair.getFirst();

            boolean good = true;
            for (Pair<Float, Integer> secondPair : returnList) {

                float curdist = fstdistfunc.distance(
                        getDataByInternalId(secondPair.getSecond()),
                        getDataByInternalId(currentPair.getSecond())
                );

                if (curdist < distToQuery) {
                    good = false;
                    break;
                }

            }
            if (good) {
                returnList.add(currentPair);
            }
        }

        for (Pair<Float, Integer> currentPair : returnList) {
            topCandidates.add(new Pair<>(-currentPair.getFirst(), currentPair.getSecond()));
        }

    }

//    void getNeighborsByHeuristic2(
//            std::priority_queue<std::pair<dist_t, tableint>, std::vector<std::pair<dist_t, tableint>>, CompareByFirst> &top_candidates,
//                const size_t M) {
//        if (top_candidates.size() < M) {
//            return;
//        }
//        std::priority_queue<std::pair<dist_t, tableint>> queue_closest;
//        std::vector<std::pair<dist_t, tableint>> return_list;
//        while (top_candidates.size() > 0) {
//            queue_closest.emplace(-top_candidates.top().first, top_candidates.top().second);
//            top_candidates.pop();
//        }
//
//        while (queue_closest.size()) {
//            if (return_list.size() >= M)
//                break;
//            std::pair<dist_t, tableint> curent_pair = queue_closest.top();
//            dist_t dist_to_query = -curent_pair.first;
//            queue_closest.pop();
//            bool good = true;
//            for (std::pair<dist_t, tableint> second_pair : return_list) {
//                dist_t curdist =
//                        fstdistfunc_(getDataByInternalId(second_pair.second),
//                                getDataByInternalId(curent_pair.second),
//                                dist_func_param_);;
//                if (curdist < dist_to_query) {
//                    good = false;
//                    break;
//                }
//            }
//            if (good) {
//                return_list.push_back(curent_pair);
//            }
//
//
//        }
//
//        for (std::pair<dist_t, tableint> curent_pair : return_list) {
//
//            top_candidates.emplace(-curent_pair.first, curent_pair.second);
//        }
//    }



    private void mutuallyConnectNewElement(float[] dataPoint,
                                           int curC,
                                           PriorityQueue<Pair<Float, Integer>> topCandidates,
                                           int level) {

        int mCurMax = level != 0 ? maxM : maxM0;

        getNeighborsByHeuristic2(topCandidates, this.m);
        if (topCandidates.size() > m) {
            throw new IllegalStateException("Should be not be more than m candidates returned by the heuristic");
        }

        List<Integer> selectedNeighbors = new ArrayList<>(m);

        while(!topCandidates.isEmpty()) {
            selectedNeighbors.add(topCandidates.remove().getSecond());
        }

        // TODO: JK no clue what to do with this

//        {
//            linklistsizeint *ll_cur;
//            if (level == 0)
//                ll_cur = get_linklist0(cur_c);
//            else
//                ll_cur = get_linklist(cur_c, level);
//
//            if (*ll_cur) {
//            throw std::runtime_error("The newly inserted element should have blank link list");
//        }


        for (int idx = 0; idx < selectedNeighbors.size(); idx++) {

            if (level > this.elementLevels.get(selectedNeighbors.get(idx))) {
                throw new IllegalStateException("Trying to make a link on a non-existent level");
            }


        }





//adaro: sorry for all the stupid questions but what does this do : tableint *data = (tableint *) (ll_cur + 1);  is that declaring an array of tableints with size  ll_cur + 1 ?
//TinoDidriksen: No, it's pointing to the single tableint at offset ll_cur+1


//adaro: So in my case if [1,2,3] was the underlying array and data is defined as tableint *data and its value is 2 , would  data[1] = 4 then change 3 to 4 in the underlying array ?
//TinoDidriksen: Yes




//        *ll_cur = selectedNeighbors.size();
//        tableint *data = (tableint *) (ll_cur + 1);

//            for (size_t idx = 0; idx < selectedNeighbors.size(); idx++) {
//                if (data[idx])
//                    throw std::runtime_error("Possible memory corruption");
//                if (level > element_levels_[selectedNeighbors[idx]])
//                    throw std::runtime_error("Trying to make a link on a non-existent level");
//
//                data[idx] = selectedNeighbors[idx];
//
//            }


    }


//    void mutuallyConnectNewElement(void *data_point, tableint cur_c,
//                                   std::priority_queue<std::pair<dist_t, tableint>, std::vector<std::pair<dist_t, tableint>>, CompareByFirst> top_candidates,
//                                   int level) {
//
//        size_t Mcurmax = level ? maxM_ : maxM0_;
//        getNeighborsByHeuristic2(top_candidates, M_);
//        if (top_candidates.size() > M_)
//            throw std::runtime_error("Should be not be more than M_ candidates returned by the heuristic");
//
//        std::vector<tableint> selectedNeighbors;
//        selectedNeighbors.reserve(M_);
//        while (top_candidates.size() > 0) {
//            selectedNeighbors.push_back(top_candidates.top().second);
//            top_candidates.pop();
//        }
//        {
//            linklistsizeint *ll_cur;
//            if (level == 0)
//                ll_cur = get_linklist0(cur_c);
//            else
//                ll_cur = get_linklist(cur_c, level);
//
//            if (*ll_cur) {
//            throw std::runtime_error("The newly inserted element should have blank link list");
//        }
//                *ll_cur = selectedNeighbors.size();
//            tableint *data = (tableint *) (ll_cur + 1);
//
//
//            for (size_t idx = 0; idx < selectedNeighbors.size(); idx++) {
//                if (data[idx])
//                    throw std::runtime_error("Possible memory corruption");
//                if (level > element_levels_[selectedNeighbors[idx]])
//                    throw std::runtime_error("Trying to make a link on a non-existent level");
//
//                data[idx] = selectedNeighbors[idx];
//
//            }
//        }
//        for (size_t idx = 0; idx < selectedNeighbors.size(); idx++) {
//
//            std::unique_lock <std::mutex> lock(link_list_locks_[selectedNeighbors[idx]]);
//
//
//            linklistsizeint *ll_other;
//            if (level == 0)
//                ll_other = get_linklist0(selectedNeighbors[idx]);
//            else
//                ll_other = get_linklist(selectedNeighbors[idx], level);
//            size_t sz_link_list_other = *ll_other;
//
//
//            if (sz_link_list_other > Mcurmax)
//                throw std::runtime_error("Bad value of sz_link_list_other");
//            if (selectedNeighbors[idx] == cur_c)
//                throw std::runtime_error("Trying to connect an element to itself");
//            if (level > element_levels_[selectedNeighbors[idx]])
//                throw std::runtime_error("Trying to make a link on a non-existent level");
//
//            tableint *data = (tableint *) (ll_other + 1);
//            if (sz_link_list_other < Mcurmax) {
//                data[sz_link_list_other] = cur_c;
//                    *ll_other = sz_link_list_other + 1;
//            } else {
//                // finding the "weakest" element to replace it with the new one
//                dist_t d_max = fstdistfunc_(getDataByInternalId(cur_c), getDataByInternalId(selectedNeighbors[idx]),
//                        dist_func_param_);
//                // Heuristic:
//                std::priority_queue<std::pair<dist_t, tableint>, std::vector<std::pair<dist_t, tableint>>, CompareByFirst> candidates;
//                candidates.emplace(d_max, cur_c);
//
//                for (size_t j = 0; j < sz_link_list_other; j++) {
//                    candidates.emplace(
//                            fstdistfunc_(getDataByInternalId(data[j]), getDataByInternalId(selectedNeighbors[idx]),
//                                    dist_func_param_), data[j]);
//                }
//
//                getNeighborsByHeuristic2(candidates, Mcurmax);
//
//                int indx = 0;
//                while (candidates.size() > 0) {
//                    data[indx] = candidates.top().second;
//                    candidates.pop();
//                    indx++;
//                }
//                    *ll_other = indx;
//                // Nearest K:
//                    /*int indx = -1;
//                    for (int j = 0; j < sz_link_list_other; j++) {
//                        dist_t d = fstdistfunc_(getDataByInternalId(data[j]), getDataByInternalId(rez[idx]), dist_func_param_);
//                        if (d > d_max) {
//                            indx = j;
//                            d_max = d;
//                        }
//                    }
//                    if (indx >= 0) {
//                        data[indx] = cur_c;
//                    } */
//            }
//
//        }
//    }


    private float[] getDataByInternalId(int internalId) {
        float[] result = new float[dataSize]; // TODO JK this seems wasteful do we really have to create a new array every time
        dataLevel0Memory.asFloatBuffer().get(result, internalId, dataSize);
        return result;
    }

//    inline char *getDataByInternalId(tableint internal_id) const {
//        return (data_level0_memory_ + internal_id * size_data_per_element_ + offsetData_);
//    }



    getLinkList0(int internalId) {

    }



    // TODO jk :looks like this is all done with direct memory access..
    // TODO data_level0_memory_ is defined as data_level0_memory_ = (char *) malloc(max_elements_ * size_data_per_element_);
    // TODO i guess i could do the same to safe on memory.. and use off heap stuff maybe


//    linklistsizeint *get_linklist0(tableint internal_id) {
//        return (linklistsizeint *) (data_level0_memory_ + internal_id * size_data_per_element_ + offsetLevel0_);
//    };
//
//    linklistsizeint *get_linklist0(tableint internal_id, char *data_level0_memory_) {
//        return (linklistsizeint *) (data_level0_memory_ + internal_id * size_data_per_element_ + offsetLevel0_);
//    };
//
//    linklistsizeint *get_linklist(tableint internal_id, int level) {
//        return (linklistsizeint *) (linkLists_[internal_id] + (level - 1) * size_links_per_element_);
//    };


    @Override
    public PriorityQueue<SearchResult<ITEM>> searchKnn(float[] vector, int k) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveIndex(OutputStream out) throws IOException {

    }

    private int getRandomLevel(double reverseSize) {
        double r = -Math.log(levelGenerator.nextDouble()) * reverseSize;
        return (int) r;
    }


    static class Node  {

        private int id;

        private List<List<Integer>> connections;

        public Node(int id, List<List<Integer>> connections) {
            this.id = id;
            this.connections = connections;
        }

        public int maxLayer() {
            return this.connections.size() - 1;
        }
    }

}
