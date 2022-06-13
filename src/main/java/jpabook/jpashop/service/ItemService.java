package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    //업데이트 변경 감지 --> merge 비추(merge는 바꿔 치기라, null 들어갈 위헙 업)
    @Transactional
    public void updateItem(Long itemId, Book book){
        Book findItem = (Book)itemRepository.findOne(itemId); //영속상태 --> save 따로 할필요 X  (@Transactional-> commit 함 -> flush-> update 쿼리날라감)
        findItem.setPrice(book.getPrice());
        findItem.setName(book.getName());
        findItem.setStockQuantity(book.getStockQuantity());
        findItem.setAuthor(book.getAuthor());
        findItem.setIsbn(book.getIsbn());
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }
    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}